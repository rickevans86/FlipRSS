// FlipRSS Reader Bridge, Codex #40. No page-to-native API, credentials or hidden JSON extraction.
(() => {
  'use strict';
  if (window.top !== window) return;
  const port = browser.runtime.connectNative('fliprss_reader');
  let stop = () => {}; let currentNonce = null;
  const clean = text => text.replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, '').replace(/\s+/g, ' ').trim();
  const noise = /(^|[ _-])(ads?|advert(isement|ising)?|social|share|sharing|related|recommended|newsletter|comments?|cookie|navigation|menu)([ _-]|$)/i;
  function visible(element, root) {
    if (!element.getClientRects().length) return false;
    for (let node = element; node && node.nodeType === 1; node = node.parentElement) {
      const style = getComputedStyle(node);
      if (node.hidden || node.inert || node.getAttribute('aria-hidden') === 'true' || style.display === 'none' ||
          style.visibility === 'hidden' || style.visibility === 'collapse' || Number(style.opacity) === 0 ||
          node.matches('nav,aside,footer,header,form,figure,figcaption,script,style,noscript,[role=dialog],[aria-modal=true]') ||
          noise.test(`${node.id} ${node.className} ${node.getAttribute('data-testid') || ''}`)) return false;
      if (node === root) break;
    }
    return true;
  }
  function extract() {
    // A modal consent/login dialog needs user interaction in the browser first.
    const blocked = [...document.querySelectorAll('[aria-modal=true]')].some(el => {
      const style = getComputedStyle(el);
      return el.getClientRects().length && style.display !== 'none' && style.visibility !== 'hidden' && Number(style.opacity) !== 0;
    });
    if (blocked) return {url: location.href.slice(0, 8192), canonical: '', title: document.title.slice(0, 2000), paragraphs: []};
    const groups = [
      '[itemprop=articleBody],.entry-content,.article-content,.article-body,.article__body,.story-body,.wp-block-post-content,[data-testid=article-body]',
      'article', 'main,[role=main]'
    ];
    let best = [], bestRoot = null;
    for (const selector of groups) {
      for (const root of document.querySelectorAll(selector)) {
        if (!visible(root, document.documentElement)) continue;
        const paragraphs = []; let size = 0;
        for (const block of root.querySelectorAll('p,h2,h3,h4,li,blockquote,pre')) {
          if (!visible(block, document.documentElement)) continue;
          if (block.parentElement?.closest('p,li,blockquote,pre') && root.contains(block.parentElement.closest('p,li,blockquote,pre'))) continue;
          const text = clean(block.innerText || '');
          if (!text || (text.length < 25 && !/^H[2-4]$/.test(block.tagName))) continue;
          if ([...block.querySelectorAll('a')].reduce((n, link) => n + (link.innerText || '').length, 0) >= text.length * .6) continue;
          if (paragraphs.length >= 500 || size + text.length > 200000) break;
          paragraphs.push(text); size += text.length;
        }
        if (size > best.reduce((n, p) => n + p.length, 0)) { best = paragraphs; bestRoot = root; }
      }
      if (best.length >= 2 && best.join('').length >= 300) break;
    }
    const articleRoot = bestRoot?.closest('article') || bestRoot;
    const heading = [...(articleRoot?.querySelectorAll('h1') || [])].find(el => visible(el, document.documentElement));
    const title = heading?.innerText || document.querySelector('meta[property="og:title"]')?.content || [...document.querySelectorAll('h1')].find(el => visible(el, document.documentElement))?.innerText || document.title;
    return {url: location.href.slice(0, 8192), canonical: document.querySelector('link[rel=canonical]')?.href?.slice(0, 8192) || '',
      title: clean(title || '').slice(0, 2000), paragraphs: best};
  }
  port.onMessage.addListener(request => {
    if (request?.type === 'cancel') { if (request.nonce === currentNonce) stop(); return; }
    if (request?.type !== 'extract' || typeof request.nonce !== 'string' || !/^[a-f0-9-]{36}$/.test(request.nonce)) return;
    stop(); currentNonce = request.nonce;
    const started = Date.now(), deadline = Math.min(18000, Math.max(1000, Number(request.deadline) || 18000));
    const minWait = Math.min(6000, Math.max(0, Number(request.minWait) || 0));
    const settle = Math.min(2000, Math.max(250, Number(request.settle) || 800));
    const minimum = Math.min(200000, Math.max(300, Number(request.minimum) || 300));
    let changed = started, previous = '', best = null, ended = false;
    function finish() {
      if (ended) return; ended = true; clearInterval(timer);
      port.postMessage({type: 'articleText', nonce: request.nonce, ...(best || extract())});
    }
    function sample() {
      const result = extract(), text = result.paragraphs.join('\n'), now = Date.now();
      if (result.paragraphs.length >= 2 && text.length >= minimum) {
        if (text !== previous) { previous = text; changed = now; }
        if (!best || text.length > best.paragraphs.join('\n').length) best = result;
        if (now - started >= minWait && now - changed >= settle) { finish(); return; }
      }
      if (now - started >= deadline) finish();
    }
    const timer = setInterval(sample, 250);
    stop = () => { ended = true; clearInterval(timer); };
    sample();
  });
  port.onDisconnect.addListener(() => stop());
})();
