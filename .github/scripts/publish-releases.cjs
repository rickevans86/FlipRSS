// AI: ChatGPT Codex, #12. Upload already signed, checksum-verified APKs; never expose signing keys.
module.exports = async ({github, context, core, manifest}) => {
  const fs = require('node:fs');
  const crypto = require('node:crypto');
  const {owner, repo} = context.repo;
  const previews = manifest ?? JSON.parse(fs.readFileSync('.release/previews.json', 'utf8'));
  for (const item of previews) {
    if (!/^[a-f0-9]{40}$/.test(item.artifact_commit) || !/^[a-f0-9]{40}$/.test(item.source_commit) ||
        !/^[a-f0-9]{64}$/.test(item.sha256) || !['preview', 'release-download'].includes(item.directory) || !/^FlipRSS-[\w.-]+\.apk$/.test(item.apk) || !/^v[\w.-]+$/.test(item.tag)) {
      throw new Error('Invalid release manifest');
    }
    const download = async (name) => {
      const {data: metadata} = await github.rest.repos.getContent({owner, repo, path: `${item.directory}/${name}`, ref: item.artifact_commit});
      const {data: blob} = await github.rest.git.getBlob({owner, repo, file_sha: metadata.sha});
      return Buffer.from(blob.content, 'base64');
    };
    const apk = await download(item.apk);
    if (crypto.createHash('sha256').update(apk).digest('hex') !== item.sha256) throw new Error('APK checksum mismatch');
    let release;
    try { release = (await github.rest.repos.getReleaseByTag({owner, repo, tag: item.tag})).data; }
    catch (error) {
      if (error.status !== 404) throw error;
      release = (await github.rest.repos.createRelease({owner, repo, tag_name: item.tag,
        target_commitish: item.source_commit, name: `FlipRSS ${item.tag}`, prerelease: true, make_latest: 'false',
        body: `${item.description}\n\nAndroid 8.0+. Prototype test signing identity. AI: ChatGPT Codex.\nSource: ${item.source_commit}\nSHA-256: ${item.sha256}\n\n[Download APK](https://github.com/${owner}/${repo}/blob/${item.artifact_commit}/${item.directory}/${item.apk}?raw=true)`})).data;
    }
    const assets = (await github.rest.repos.listReleaseAssets({owner, repo, release_id: release.id, per_page: 100})).data;
    for (const [name, bytes] of [[item.apk, apk], ['SHA256SUMS.txt', Buffer.from(`${item.sha256}  ${item.apk}\n`)], ['build-info.json', await download('build-info.json')]]) {
      const expected = 'sha256:' + crypto.createHash('sha256').update(bytes).digest('hex');
      const existing = assets.find(asset => asset.name === name);
      if (existing) {
        if (existing.size !== bytes.length || (existing.digest && existing.digest !== expected)) throw new Error(`Existing asset mismatch: ${name}`);
        core.info(`Already present: ${item.tag}/${name}`); continue;
      }
      await github.rest.repos.uploadReleaseAsset({owner, repo, release_id: release.id, name,
        data: bytes, headers: {'content-type': name.endsWith('.apk') ? 'application/vnd.android.package-archive' : 'application/octet-stream', 'content-length': bytes.length}});
      core.info(`Uploaded: ${item.tag}/${name}`);
    }
  }
};
