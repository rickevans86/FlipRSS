const test = require('node:test');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const publish = require('./publish-releases.cjs');
function fixture() {
  const bytes = Buffer.from('test APK bytes');
  const item = {tag:'v0.1-test',artifact_commit:'a'.repeat(40),source_commit:'b'.repeat(40),directory:'preview',apk:'FlipRSS-test.apk',sha256:crypto.createHash('sha256').update(bytes).digest('hex'),description:'Test'};
  const assets = []; const created = [];
  const github = {rest:{repos:{
    getContent: async ({path}) => ({data:{sha:path.endsWith('.apk') ? 'apk' : 'info'}}),
    getReleaseByTag: async () => { throw {status:404}; },
    createRelease: async (data) => {created.push(data);return {data:{id:1}};},
    listReleaseAssets: async () => ({data:assets}),
    updateRelease: async () => ({data:{id:1}}),
    uploadReleaseAsset: async ({name,data}) => {assets.push({name,size:data.length,digest:'sha256:'+crypto.createHash('sha256').update(data).digest('hex')});}
  },git:{getBlob: async ({file_sha}) => ({data:{content:(file_sha==='apk'?bytes:Buffer.from('{}')).toString('base64')}})}}};
  return {github,context:{repo:{owner:'test',repo:'repo'}},core:{info:()=>{}},manifest:[item],assets,created};
}
test('creates prerelease and uploads verified assets, then safely skips existing assets', async () => {
  const f=fixture(); await publish(f);
  assert.equal(f.assets.length,3);assert.equal(f.created[0].prerelease,true);assert.equal(f.created[0].target_commitish,'b'.repeat(40));
  f.github.rest.repos.getReleaseByTag=async()=>({data:{id:1}});
  await publish(f);assert.equal(f.assets.length,3);
});
test('checksum mismatch blocks publication', async () => {
  const f=fixture();f.manifest[0].sha256='0'.repeat(64);
  await assert.rejects(publish(f),/checksum mismatch/);assert.equal(f.created.length,0);assert.equal(f.assets.length,0);
});
test('does not overwrite an existing mismatched APK', async () => {
  const f=fixture();f.assets.push({name:'FlipRSS-test.apk',size:1});
  f.github.rest.repos.getReleaseByTag=async()=>({data:{id:1}});
  await assert.rejects(publish(f),/Existing asset mismatch/);assert.equal(f.assets.length,1);
});
test('rejects artifact path traversal', async () => {
  const f=fixture();f.manifest[0].directory='../private';
  await assert.rejects(publish(f),/Invalid release manifest/);assert.equal(f.created.length,0);
});
