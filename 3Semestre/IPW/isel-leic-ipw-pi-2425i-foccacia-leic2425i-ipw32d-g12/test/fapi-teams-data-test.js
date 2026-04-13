import teamsDataInit from "../data/memory/fapi-teams-data.mjs";
import assert from "node:assert";

const teamsDataTest = teamsDataInit();

describe('Groups Functions', () => {
    it('Should add a team from the API FOOTBALL', async () => {
        const team = await teamsDataTest.getTeamByName('Juventus');
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
      it('Should not add a team from the API FOOTBALL because of name', async () => {
        const team = await teamsDataTest.getTeamByName('HIGHEIGAIGIAEHGIAEHGIAEHIGHEAG');
        assert.deepStrictEqual(team, [], 'Should return an empty array because team doesnt exist');
      });
      it('Should get the leagues of a team from the API FOOTBALL', async () => {
        const leagues = await teamsDataTest.getLeaguesByTeamId(496, 2022);
        const leaguesToCompare = [
            { id: 2, name: 'UEFA Champions League', season: 2022 },
            { id: 667, name: 'Friendlies Clubs', season: 2022 },
            { id: 135, name: 'Serie A', season: 2022 },
            { id: 137, name: 'Coppa Italia', season: 2022 },
            { id: 3, name: 'UEFA Europa League', season: 2022 }
          ];
        assert.deepStrictEqual(leagues, leaguesToCompare, 'Should be the same leagues');
      });
      it('Should not get the leagues of a team from the API FOOTBALL beacuse of year off limits', async () => {
        const leagues = await teamsDataTest.getLeaguesByTeamId(496, 2024);
        const leaguesToCompare = [];
        assert.deepStrictEqual(leagues, leaguesToCompare, 'Should be an empty array');
      });
    })