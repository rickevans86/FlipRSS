const {test, before, after} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const {chromium} = require('playwright');
const source = fs.readFileSync('app/src/main/assets/reader-bridge/reader.js','utf8');
const nonce='12345678-1234-1234-1234-123456789abc';
const title='Trump, Putin and Xi talk nuclear weapons and Ukraine';
const paragraph='This is visible reporting and background from the expanded article, added by browser rendering after the feed supplied only a short excerpt.';
const body=Array.from({length:5},(_,i)=>`<p>Paragraph ${i}: ${paragraph}</p>`).join('');
let browser;
before(async()=>{browser=await chromium.launch({headless:true,...(process.env.CHROMIUM_PATH?{executablePath:process.env.CHROMIUM_PATH}:{}),args:['--no-sandbox']});});
after(async()=>{await browser?.close();});
async function fixture(html, viewport={width:390,height:740}) {
 const page=await browser.newPage({viewport});
 await page.route('**/*', route=>route.fulfill({contentType:'text/html',body:`<!doctype html><title>${title}</title><meta property="og:title" content="${title}"><link rel="canonical" href="https://www.theage.com.au/world/story"><style>body{font-size:18px}</style>${html}`}));
 await page.goto('https://www.theage.com.au/world/story');
 await page.evaluate(()=>{
  window.messages=[];window.nativeRequest=null;
  window.browser={runtime:{connectNative:()=>({onMessage:{addListener:fn=>window.nativeRequest=fn},onDisconnect:{addListener:fn=>window.disconnected=fn},postMessage:msg=>window.messages.push(msg)})}};
 });
 await page.addScriptTag({content:source});return page;
}
async function request(page, extra={}) {
 await page.evaluate(message=>window.nativeRequest(message),{type:'extract',nonce,minimum:300,minWait:0,settle:250,deadline:2500,...extra});
 await page.waitForFunction(()=>window.messages.length>0,{timeout:6000});return page.evaluate(()=>window.messages.at(-1));
}
test('extracts rendered paragraphs despite restricted metadata; excludes hidden/script/ads/forms/comments',async()=>{
 const page=await fixture(`<script type="application/ld+json">{"isAccessibleForFree":false,"articleBody":"SECRET JSON"}</script><nav><p>NAVIGATION</p></nav><article><h1>${title}</h1><div class="paywall-content">${body}<p hidden>HIDDEN TEXT</p><div style="display:none"><p>SECRET CSS</p></div><p style="opacity:0">TRANSPARENT SECRET</p><aside><p>BUY AN AD</p></aside><div data-testid="ad"><p>SPONSORED ADVERTISING COPY THAT MUST NOT BE INCLUDED IN THE ARTICLE</p></div><form><p>EMAIL ADDRESS</p></form><div class="comments"><p>USER COMMENT</p></div></div></article>`);
 try { const result=await request(page);assert.equal(result.paragraphs.length,5);assert.equal(result.nonce,nonce);for(const word of ['SECRET','HIDDEN','NAVIGATION','BUY AN AD','SPONSORED','EMAIL ADDRESS','USER COMMENT'])assert.ok(!result.paragraphs.join(' ').includes(word));assert.ok(result.title.includes('Trump')); }finally{await page.close();}
});
test('waits for delayed add-on DOM replacement and captures all newly inserted paragraphs',async()=>{
 const page=await fixture(`<article><h1>${title}</h1><div class="article-body"><p>Short excerpt.</p></div></article>`);
 try { await page.evaluate(html=>setTimeout(()=>document.querySelector('.article-body').innerHTML=html,550),body);const result=await request(page,{minWait:1000,deadline:3500});assert.equal(result.paragraphs.length,5);assert.ok(result.paragraphs.at(-1).startsWith('Paragraph 4')); }finally{await page.close();}
});
test('keeps paragraphs below viewport and avoids duplicate nested blocks',async()=>{
 const page=await fixture(`<article><h1>${title}</h1>${body}<blockquote><p>A quoted paragraph. ${paragraph}</p></blockquote></article>`,{width:390,height:1});
 try {const result=await request(page);assert.equal(result.paragraphs.length,6);assert.equal(result.paragraphs.filter(p=>p.includes('quoted')).length,1);}finally{await page.close();}
});
test('a visible modal blocks capture until user/add-on removes it',async()=>{
 const page=await fixture(`<article><h1>${title}</h1>${body}</article><div aria-modal="true">Login or consent required</div>`);
 try {await page.evaluate(()=>setTimeout(()=>document.querySelector('[aria-modal]').remove(),500));const result=await request(page,{minWait:800});assert.equal(result.paragraphs.length,5);}finally{await page.close();}
});
test('cancelled requests and invalid nonces produce no native payload',async()=>{
 const page=await fixture(`<article>${body}</article>`);
 try {await page.evaluate(n=>{window.nativeRequest({type:'extract',nonce:'bad'});window.nativeRequest({type:'extract',nonce:n,minWait:2000});window.nativeRequest({type:'cancel',nonce:n});},nonce);await page.waitForTimeout(1200);assert.equal(await page.evaluate(()=>window.messages.length),0);}finally{await page.close();}
});
test('disconnect stops extraction and article text remains bounded',async()=>{
 const page=await fixture(`<article><h1>${title}</h1>${Array.from({length:600},(_,i)=>`<p>${i} ${paragraph}</p>`).join('')}</article>`);
 try {const result=await request(page);assert.ok(result.paragraphs.length<=500);assert.ok(result.paragraphs.join('').length<=200000);await page.evaluate(n=>{window.nativeRequest({type:'extract',nonce:n,minWait:2000});window.disconnected();},nonce);await page.waitForTimeout(700);assert.equal(await page.evaluate(()=>window.messages.length),1);}finally{await page.close();}
});
