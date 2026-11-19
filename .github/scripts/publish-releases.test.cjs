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
  const f=fixture();f.assets.push({name:'FlipRSS.apk',size:1});
  f.github.rest.repos.getReleaseByTag=async()=>({data:{id:1}});
  await assert.rejects(publish(f),/Existing asset mismatch/);assert.equal(f.assets.length,1);
});
test('rejects artifact path traversal', async () => {
  const f=fixture();f.manifest[0].directory='../private';
  await assert.rejects(publish(f),/Invalid release manifest/);assert.equal(f.created.length,0);
});

test('regular release publishes the standard filename and becomes latest', async () => {
  const f=fixture();f.manifest[0].prerelease=false;let updated;
  f.github.rest.repos.updateRelease=async(data)=>{updated=data;return {data:{id:1}};};
  await publish(f);
  assert.equal(f.created[0].prerelease,false);assert.equal(f.created[0].make_latest,'true');
  assert.equal(f.assets[0].name,'FlipRSS.apk');assert.equal(updated.prerelease,false);assert.equal(updated.make_latest,'true');
});

function partsFixture() {
  const f = fixture(); const chunks = [Buffer.from('test APK '), Buffer.from('bytes')];
  const digest = bytes => crypto.createHash('sha256').update(bytes).digest('hex');
  f.manifest[0].apk_parts = chunks.map((bytes, i) => ({name:`${f.manifest[0].apk}.part${String(i+1).padStart(3,'0')}`,sha256:digest(bytes)}));
  f.github.rest.repos.getContent = async ({path}) => ({data:{sha:path.split('/').pop()}});
  f.github.rest.git.getBlob = async ({file_sha}) => ({data:{content:(file_sha.endsWith('part001')?chunks[0]:file_sha.endsWith('part002')?chunks[1]:Buffer.from('{}')).toString('base64')}});
  return f;
}
test('reconstructs larger APK from ordered immutable verified chunks', async () => {
  const f = partsFixture(); await publish(f);
  assert.equal(f.assets[0].name,'FlipRSS.apk'); assert.equal(f.assets[0].digest,'sha256:'+f.manifest[0].sha256);
});
test('bad part hash blocks release creation', async () => {
  const f=partsFixture(); f.manifest[0].apk_parts[1].sha256='0'.repeat(64);
  await assert.rejects(publish(f),/part checksum/); assert.equal(f.created.length,0);
});
test('reordered, missing, excessive or path-traversal parts are rejected', async () => {
  for (const parts of [[], [{name:'../private',sha256:'a'.repeat(64)}], Array(17).fill({name:'x',sha256:'a'.repeat(64)})]) {
    const f=partsFixture(); f.manifest[0].apk_parts=parts;
    await assert.rejects(publish(f),/Invalid APK parts/); assert.equal(f.created.length,0);
  }
  const f=partsFixture(); f.manifest[0].apk_parts.reverse();
  await assert.rejects(publish(f),/Invalid APK parts/); assert.equal(f.created.length,0);
});
test('valid parts still require the final assembled APK hash', async () => {
  const f=partsFixture(); f.manifest[0].sha256='0'.repeat(64);
  await assert.rejects(publish(f),/APK checksum mismatch/); assert.equal(f.created.length,0);
});
