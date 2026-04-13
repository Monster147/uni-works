import foccaciaDataInit from "../data/memory/foccacia-data-mem.mjs"
import { GROUPS } from "../data/memory/foccacia-data-mem.mjs";
import { USERS } from "../data/memory/foccacia-data-mem.mjs";
import { TEAMS } from "../data/memory/foccacia-data-mem.mjs";
import assert from "node:assert";
import {errors} from "../common/errors.mjs"

const foccaciaDataTest = foccaciaDataInit();

describe('Groups Functions', () => {
  it('Should get all groups', async () => {
    const groups = await foccaciaDataTest.getAllGroups(1);
    const groupsCompare = GROUPS.filter(group => group.userId == 1)
    assert.deepEqual(groups, groupsCompare, 'Should return all groups from user 1');
    assert.deepEqual(groups.length, groupsCompare.length, 'Should be the same lenght');
  })
  it('Should get a group by Id and userId', async () => {
    const group = await foccaciaDataTest.getGroup(3);
    assert.strictEqual(group, GROUPS[2], 'Should return correct group');
  });
  it('Should not get a group by wrong pair of Id and userId', async () => {
    const group = await foccaciaDataTest.getGroup(4);
    assert.strictEqual(group, undefined, 'Should return undefined for wrong groupId');
  });
  const newGroup = {
    name: 'Test',
    description: 'Test',
  }
  it('Should create a group by groupData and userId', async () => {
    const newGroupCompare = {
      id: 4,
      name: 'Test',
      description: 'Test',
      userId: 3,
      teams: [], 
    };
    const group = await foccaciaDataTest.createGroup(newGroup, 3);
    assert.deepEqual(group, newGroupCompare, 'Should return the new created group');
  });
  it('Should update a group by Id, groupData and userId', async () => {
    const groupCompare = {
      id: 2,
      name: 'Test',
      description: 'Test',
      userId: 2,
      teams: [TEAMS[1], TEAMS[2]], 
    };
    const group = await foccaciaDataTest.updateGroup(2, newGroup, 2);
    assert.deepStrictEqual(group, groupCompare, 'Should return the update');
  });
  it('Should not update group by wrong Id and userId', async () => {
    await assert.rejects(
      foccaciaDataTest.updateGroup(5, newGroup),
      errors.GROUP_NOT_FOUND(5),
      'Should throw an error for wrong groupId'
    );
  });
  it('Should delete a group by Id and userId', async () => {
    const groupCompare = {
      id: 4,
      name: 'Test',
      description: 'Test',
      userId: 3,
      teams: [], 
    }
    const group = await foccaciaDataTest.deleteGroup(4, 3);
    assert.deepEqual(group, groupCompare, 'Should return the deleted group');
  });
  it('Should not delete group by wrong Id or userId', async () => {
    await assert.rejects(
      foccaciaDataTest.deleteGroup(1, 2),
      errors.GROUP_NOT_FOUND(1),
      'Should throw an error for wrong Id or unauthorized userId'
    );
  });
});

describe('User Functions', () => {
  it('Should get a user ID by token', async () => {
    const userId =  await foccaciaDataTest.getUserId('b0506867-77c3-4142-9437-1f627deebd67');
    assert.strictEqual(userId, 1, 'Should return correct user ID');
  });
  it('Should not get a user ID by inexistent token', async () => {
    await assert.rejects(
      foccaciaDataTest.getUserId('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxx'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for wrong token'
    );
  });
  it('Should get user by name', async () => {
    const user = await foccaciaDataTest.getUserByName('test');
    assert.deepStrictEqual(user, USERS[2], 'Should return correct user');
  });
  it('Should not get user by inexistent name', async () => {
    await assert.rejects(
      foccaciaDataTest.getUserByName('test1'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for inexistent name'
    );
  });
  it('Should get token by userId', async () => {
    const token = await foccaciaDataTest.getTokenByUserId(3);
    assert.strictEqual(token, '123-456-789', 'Should return correct token');
  });
  it('Should not get token by inexistent userId', async () => {
    await assert.rejects(
      foccaciaDataTest.getTokenByUserId(4),
      errors.USER_NOT_FOUND(),
      'Should throw an error for inexistent userId'
    );
  });
  it('Should add a new user', async () => {
    const newUser = await foccaciaDataTest.addUser('newUser');
    assert.ok(newUser, 'User should be added successfully');
    assert.strictEqual(newUser.name, 'newUser');
  });
  it('Should not add a duplicate user', async () => {
    await assert.rejects(
      foccaciaDataTest.addUser('jsaldanha'),
      errors.DUPLICATE_USER('jsaldanha'),
      'Should throw an error for duplicate user'
    );
  });
  it('Should get user by token', async () => {
    const user = await foccaciaDataTest.getUser({token: '123-456-789'});
    assert.deepEqual(user, USERS[2], 'Should return correct user');
  });
  it('Should not get user by inexistent token', async () => {
    await assert.rejects(
      foccaciaDataTest.getUser('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxx'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for inexistent token'
    );
  });
});

describe('Teams Functions', () =>{
  it('Should add a team to a group by groupId, teamData and userId', async () => {
    const teamToAdd = {id: 6, name: 'Test3'}
    const team = await foccaciaDataTest.addTeamToGroup(3, teamToAdd, 3);
    assert.deepStrictEqual(team, TEAMS[5], 'Should return correct team');
  });
  it('Should not add a team to a group by wrong groupId', async () => {
    await assert.rejects(
      foccaciaDataTest.addTeamToGroup(4, 6, 3),
      errors.GROUP_NOT_FOUND(4),
      'Should throw an error for adding a team to inexistent group'
    );
  });
  it('Should not add a team to a group by wrong teamData', async () => {
    const teamToAdd = {id: 10, name: 'Test7'}
    await assert.rejects(
      foccaciaDataTest.addTeamToGroup(3, teamToAdd, 3),
      errors.TEAM_NOT_FOUND('Test7'),
      'Should throw an error for trying to add an inexistent team'
    );
  });
  it('Should not add a team to a group by existing team in that group', async () => {
    const teamToAdd = {id: 5, name: 'Test2'}
    await assert.rejects(
      foccaciaDataTest.addTeamToGroup(3, teamToAdd, 3),
      errors.DUPLICATE_TEAM('Test2'),
      'Should throw an error for adding a team that already exists in that group'
    );
  });
  it('Should remove a team from a group by groupId, teamId and userId', async () => {
    const team = await foccaciaDataTest.removeTeamFromGroup(3, 5);
    assert.deepStrictEqual(team, TEAMS[4], 'Should return correct team');
  });
  it('Should not remove a team from a group by wrong groupId', async () => {
    await assert.rejects(
      foccaciaDataTest.removeTeamFromGroup(4, 5),
      errors.GROUP_NOT_FOUND(4),
      'Should throw an error for trying to remove a team that does not exist in said group'
    );
  });
  it('Should not remove a team from a group by wrong teamId', async () => {
    await assert.rejects(
      foccaciaDataTest.removeTeamFromGroup(3, 0),
      errors.TEAM_NOT_FOUND(0),
      'Should throw an error for trying to remove a team that does not exist in that group'
    );
  });
  it('Should get all teams', async () => {
    const teams = await foccaciaDataTest.getAllTeams();
    assert.deepStrictEqual(teams, TEAMS, 'Should return all teams');
  });
})
