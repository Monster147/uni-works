import foccaciaServicesInit from "../services/foccacia-services.mjs";
import foccaciaDataInit from "../data/memory/foccacia-data-mem.mjs";
import teamsDataInit from "../data/memory/fapi-teams-data.mjs";
import { GROUPS } from "../data/memory/foccacia-data-mem.mjs";
import { USERS } from "../data/memory/foccacia-data-mem.mjs";
import { TEAMS } from "../data/memory/foccacia-data-mem.mjs";
import assert from "node:assert";
import {errors} from "../common/errors.mjs"

const groupsDataTest = foccaciaDataInit();
const usersDataTest = foccaciaDataInit();
const teamsDataTest = teamsDataInit();
const foccaciaServicesTest = foccaciaServicesInit(teamsDataTest, usersDataTest, groupsDataTest);

describe('Groups Functions', () => {
  it('Should get all groups of a valid user', async () => {
    const groups = await foccaciaServicesTest.getAllGroups('b0506867-77c3-4142-9437-1f627deebd67');
    const groupsCompare = GROUPS.filter(group => group.userId == 1);
    assert.deepStrictEqual(groups, groupsCompare, 'Should return all groups associated with a user');
    assert.deepStrictEqual(groups.length, groupsCompare.length, 'Should be the same lenght');
  })
  it('Should not get all groups of a invalid user', async () => {
    await assert.rejects(
      foccaciaServicesTest.getAllGroups('xxxxxx-xxxxxxxx-xxxxxx'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for user not found beacuse token doesnt exist'
    );
  })
  it('Should get a group by Id and userId', async () => {
    const group = await foccaciaServicesTest.getGroup(1, 'b0506867-77c3-4142-9437-1f627deebd67');
    assert.deepEqual(group, GROUPS[0], 'Should return correct group');
  });
  it('Should not get a group by wrong pair of Id and userId', async () => {
    await assert.rejects(
      foccaciaServicesTest.getGroup(3, 'b0506867-77c3-4142-9437-1f627deebd67'),
      errors.GROUP_NOT_FOUND(3),
      'Should throw an error for group not found or unauthorized access to said group'
    );
  });
  it('Should create a group by groupData and userId', async () => {
    const newGroup = {
      name: 'Test',
      description: 'Test',
    }
    const newGroupCompare = {
      id: 4,
      name: 'Test',
      description: 'Test',
      userId: 3,
      teams: [], 
    };
    const group = await foccaciaServicesTest.addGroup(newGroup, '123-456-789');
    assert.deepEqual(group, newGroupCompare, 'Should return the new created group');
  });
  it('Should update a group by Id, groupData and userId', async () => {
    const newGroup = {
      name: 'Test',
      description: 'Test',
    }
    const groupCompare = {
      id: 2,
      name: 'Test',
      description: 'Test',
      userId: 2,
      teams: [TEAMS[1], TEAMS[2]], 
    };
    const group = await foccaciaServicesTest.updateGroup(2, newGroup, 'f1d1cdbc-97f0-41c4-b206-051250684b19');
    assert.deepStrictEqual(group, groupCompare, 'Should return the updated group');
  });
  it('Should not update group by wrong Id and userId', async () => {
    const newGroup = {
      name: 'Test',
      description: 'Test',
    }
    await assert.rejects(
      foccaciaServicesTest.updateGroup(1, newGroup, 'f1d1cdbc-97f0-41c4-b206-051250684b19'),
      errors.GROUP_NOT_FOUND(1),
      'Should throw an error for group not found or unauthorized access to said group'
    );
  });
  it('Should delete a group by Id and userId', async() => {
    const groupCompare = {
      id: 4,
      name: 'Test',
      description: 'Test',
      userId: 3,
      teams: [], 
    }
    const group = await foccaciaServicesTest.deleteGroup(4, '123-456-789');
    assert.deepEqual(group, groupCompare, 'Should return the deleted group');
  });
  it('Should not delete group by wrong Id or userId', async () => {
    await assert.rejects(
      foccaciaServicesTest.deleteGroup(1, 'f1d1cdbc-97f0-41c4-b206-051250684b19'),
      errors.GROUP_NOT_FOUND(1),
      'Should throw an error for group not found or unauthorized access to said group'
    );
  });
});

describe('User Functions', () => {
  it('Should get a user ID by token', async () => {
    const userId = await foccaciaServicesTest.getUserId('b0506867-77c3-4142-9437-1f627deebd67');
    assert.strictEqual(userId, 1, 'Should return correct user ID');
  });
  it('Should not get a user ID by inexistent token', async () => {
    await assert.rejects(
      foccaciaServicesTest.getUserId('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxx'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for wrong token'
    );
  });
  it('Should get token by userId', async () => {
    const token = await foccaciaServicesTest.getTokenbyUserId(3);
    assert.strictEqual(token, '123-456-789', 'Should return correct token');
  });
  it('Should not get token by inexistent userId', async () => {
    await assert.rejects(
      foccaciaServicesTest.getTokenbyUserId(4),
      errors.USER_NOT_FOUND(),
      'Should throw an error for inexistent userId'
    );
  });
  it('Should add a new user', async () => {
    const newUser = await foccaciaServicesTest.addUser('newUser');
    assert.ok(newUser, 'User should be added successfully');
    assert.strictEqual(newUser.name, 'newUser');
  });
  it('Should not add a duplicate user', async () => {
    await assert.rejects(
      foccaciaServicesTest.addUser('jsaldanha'),
      errors.DUPLICATE_USER('jsaldanha'),
      'Should throw an error for duplicate user'
    );
  });
  it('Should get user by token', async () => {
    const user = await foccaciaServicesTest.getUser('123-456-789');
    assert.deepStrictEqual(user, USERS[2], 'Should return correct user');
  });
  it('Should not get user by inexistent token', async () => {
    await assert.rejects(
      foccaciaServicesTest.getUser('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxx'),
      errors.USER_NOT_FOUND(),
      'Should throw an error for inexistent token'
    );
  });
  it('Should create a new user', async () => {
    const userToCreate = {id: 4, userName: 'NewUser', password: '123'}
    const newUser = await foccaciaServicesTest.createUser(userToCreate);
    assert.ok(newUser, 'User should be added successfully');
    assert.strictEqual(newUser.name, 'NewUser');
  });
  it('Should not create a new user with same name', async () => {
    const userToCreate = {id: 4, userName: 'jsaldanha', password: '123456'}
    await assert.rejects(
      foccaciaServicesTest.createUser(userToCreate),
      errors.DUPLICATE_USER(userToCreate.userName),
      'Should throw an error for duplicate user'
    );
  });
});

describe('Teams Functions', () =>{
  it('Should add a team to a group by groupId, teamId and userId', async () => {
    const teamToAdd = {id: 6, name: 'Test3'};
    const team = await foccaciaServicesTest.addTeamToGroup(3, teamToAdd, '123-456-789');
    assert.deepStrictEqual(team, TEAMS[5], 'Should return correct team');
  });
  it('Should not add a team to a group by wrong groupId', async () => {
    const teamToAdd = {id: 4, name: 'Test1'};
    await assert.rejects(
      foccaciaServicesTest.addTeamToGroup(4, teamToAdd, '123-456-789'),
      errors.GROUP_NOT_FOUND(4),
      'Should throw an error for adding a team to inexistent group'
    );
  });
  it('Should not add a team to a group by wrong teamId', async () => {
    const teamToAdd = {id: 10, name: 'XXXXXX'};
    await assert.rejects(
      foccaciaServicesTest.addTeamToGroup(3, teamToAdd, '123-456-789'),
      errors.TEAM_NOT_FOUND('XXXXXX'),
      'Should throw an error for trying to add an inexistent team'
    );
  });
  it('Should not add a team to a group by existing team in that group', async () => {
    const teamToAdd = {id: 5, name: 'Test2'};
    await assert.rejects(
      foccaciaServicesTest.addTeamToGroup(3, teamToAdd, '123-456-789'),
      errors.DUPLICATE_TEAM('Test2'),
      'Should throw an error for trying to add a duplicate team to a group'
    );
  });
  it('Should remove a team from a group by groupId, teamId and userId', async () => {
    const team = await foccaciaServicesTest.removeTeamFromGroup(3, 5, '123-456-789');
    assert.deepStrictEqual(team, TEAMS[4], 'Should return correct team');
  });
  it('Should not remove a team from a group by wrong groupId', async () => {
    await assert.rejects(
      foccaciaServicesTest.removeTeamFromGroup(4, 5, '123-456-789'),
      errors.GROUP_NOT_FOUND(4),
      'Should throw an error for trying to remove from inexistent group'
    );
  });
  it('Should not remove a team from a group by wrong teamId', async () => {
    await assert.rejects(
      foccaciaServicesTest.removeTeamFromGroup(3, 0, '123-456-789'),
      errors.TEAM_NOT_FOUND(0),
      'Should throw an error for trying to remove a inexistent team a group'
    );
  });
  it('Should get all teams', async () => {
    const teams = await foccaciaServicesTest.getAllTeams();
    assert.deepStrictEqual(teams, TEAMS, 'Should return all teams');
  });
  it('Should add a team from the API FOOTBALL to teams array', async () => {
    const teamToAdd = await teamsDataTest.getTeamByName('Juventus');
    const team = await foccaciaServicesTest.addTeamFromAPItoTeams(teamToAdd);
    const teamToCompare = {
      id: 496,
      name: 'Juventus',
      logo: "https://media.api-sports.io/football/teams/496.png",
      stadium: 'Allianz Stadium',
      leagues: [
        { id: 2, name: 'UEFA Champions League', season: 2022 },
        { id: 667, name: 'Friendlies Clubs', season: 2022 },
        { id: 135, name: 'Serie A', season: 2022 },
        { id: 137, name: 'Coppa Italia', season: 2022 },
        { id: 3, name: 'UEFA Europa League', season: 2022 }
      ]
    }
    assert.deepStrictEqual(team, teamToCompare, 'Should be the same team');
  });
  it('Should not add a team from the API FOOTBALL to teams array because of teamID', async () => {
    const teamToAdd = await teamsDataTest.getTeamByName('Juventus');
    await assert.rejects(
      foccaciaServicesTest.addTeamFromAPItoTeams(teamToAdd),
      errors.DUPLICATE_TEAM('Juventus'),
      'Should throw an error for trying to add an existent team to the teams array'
    );
  });
})