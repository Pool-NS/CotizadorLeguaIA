import { readFileSync } from 'node:fs';
import { before, after, beforeEach, test } from 'node:test';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, serverTimestamp, setDoc } from 'firebase/firestore';

const projectId = 'demo-cotizador-leguia';
let environment;

before(async () => {
  environment = await initializeTestEnvironment({
    projectId,
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

after(async () => environment?.cleanup());
beforeEach(async () => environment.clearFirestore());

async function activateMember(orgId, uid) {
  await environment.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), `organizations/${orgId}/members/${uid}`), { active: true });
  });
}

function quoteRecord(uid, id = 'quote-001') {
  return {
    recordId: id,
    ownerUid: uid,
    serviceId: 1,
    materialId: null,
    lengthM: 3,
    widthM: 2,
    heightM: null,
    quoteStatus: 'PENDIENTE',
    assistanceMode: 'ai_assisted',
    startedAtEpochMs: 1_800_000_000_000,
    priceCommunicatedAtEpochMs: null,
    durationMillis: null,
    communicationConfirmed: false,
    appVersion: '1.5.0',
    deviceModel: 'Android test device',
    androidApiLevel: 35,
    networkTransport: 'wifi',
    source: 'android_room',
    uploadedAt: serverTimestamp(),
  };
}

test('denies unauthenticated quote writes', async () => {
  const db = environment.unauthenticatedContext().firestore();
  await assertFails(setDoc(doc(db, 'organizations/shop-a/quotes/quote-001'), quoteRecord('user-a')));
});

test('denies a signed-in user without active organization membership', async () => {
  const db = environment.authenticatedContext('user-a').firestore();
  await assertFails(setDoc(doc(db, 'organizations/shop-a/quotes/quote-001'), quoteRecord('user-a')));
});

test('allows an active member to create and read a minimized quote record', async () => {
  await activateMember('shop-a', 'user-a');
  const db = environment.authenticatedContext('user-a').firestore();
  const reference = doc(db, 'organizations/shop-a/quotes/quote-001');
  await assertSucceeds(setDoc(reference, quoteRecord('user-a')));
  await assertSucceeds(getDoc(reference));
});

test('rejects free-text personal data and immutable-record updates', async () => {
  await activateMember('shop-a', 'user-a');
  const db = environment.authenticatedContext('user-a').firestore();
  const reference = doc(db, 'organizations/shop-a/quotes/quote-001');
  await assertFails(setDoc(reference, { ...quoteRecord('user-a'), customerRequirement: 'texto personal' }));
  await assertSucceeds(setDoc(reference, quoteRecord('user-a')));
  await assertFails(setDoc(reference, quoteRecord('user-a')));
});

test('does not allow a member of another organization to read quote records', async () => {
  await activateMember('shop-a', 'user-a');
  await activateMember('shop-b', 'user-b');
  const ownerDb = environment.authenticatedContext('user-a').firestore();
  const outsiderDb = environment.authenticatedContext('user-b').firestore();
  const reference = doc(ownerDb, 'organizations/shop-a/quotes/quote-001');
  await assertSucceeds(setDoc(reference, quoteRecord('user-a')));
  await assertFails(getDoc(doc(outsiderDb, 'organizations/shop-a/quotes/quote-001')));
});
