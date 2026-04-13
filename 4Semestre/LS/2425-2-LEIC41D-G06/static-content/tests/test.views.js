import { getUserView } from '../sparouter/views/components/user/getUserView.js';
import { getUserRentalsEmpty } from '../sparouter/views/components/user/getUserRentalsEmpty.js';
import { getUserRentalsView } from '../sparouter/views/components/user/getUserRentalsView.js';
import { getClubsView } from '../sparouter/views/components/club/getClubsView.js';
import { getClubsByNameView } from '../sparouter/views/components/club/getClubsByNameView.js';
import { getClubView } from '../sparouter/views/components/club/getClubView.js';
import {a, div, form, h1, input, label, li, ul} from "../sparouter/dls.js";
import {evaluateTests} from "./test.utils.js";
import getHome from "../sparouter/views/getHome.js";
import {courtsListView} from "../sparouter/views/components/court/courtsListView.js";
import {emptyCourtRentalsByDateView} from "../sparouter/views/components/court/emptyCourtRentalsByDateView.js";
import {emptyCourtRentalsView} from "../sparouter/views/components/court/emptyCourtRentalsView.js";
import {emptyCourtsListView} from "../sparouter/views/components/court/emptyCourtsListView.js";
import {getAvailableHoursView} from "../sparouter/views/components/court/getAvailableHoursView.js";
import {getCourtRentalsView} from "../sparouter/views/components/court/getCourtRentalsView.js";
import {getCourtsRentalsByDateView} from "../sparouter/views/components/court/getCourtsRentalsByDateView.js";
import {getCourtView} from "../sparouter/views/components/court/getCourtView.js";
import {createRentalView} from "../sparouter/views/components/rental/createRentalView.js";
import {getRentalEmptyView} from "../sparouter/views/components/rental/getRentalEmptyView.js";
import {getRentalView} from "../sparouter/views/components/rental/getRentalView.js";
import createAccount from "../sparouter/views/account/create.js";
import createLoginLogout from "../sparouter/views/account/login-logout.js";
import notLoggedIn from "../sparouter/views/account/notLoggedIn.js";
import {createCourtView} from "../sparouter/views/components/court/createCourtView.js";
import {deleteRentalView} from "../sparouter/views/components/rental/deleteRentalView.js";
import {updateRentalView} from "../sparouter/views/components/rental/updateRentalView.js";
import {createClubView} from "../sparouter/views/components/club/createClubView.js";

describe('Views tests', function () {
    before(function () {
        //
    });
    describe('Testing Home View', () => {
        it('should render home view correctly', async function () {
            if (typeof getHome === 'function') {
                const container = document.createElement('div');
                const animP = document.createElement('p');
                animP.className = 'anim';
                document.body.appendChild(animP);

                await getHome(container);

                chai.expect(container.innerHTML).to.contain('Home');
                chai.expect(container.innerHTML).to.contain('Search Club by Name');
                animP.remove();
            } else {
                this.skip();
            }
        });
    });

    describe('Testing User Views', () => {
        it('should render user details view correctly', async function() {
            if (typeof getUserView === 'function') {
                const mainContent = document.createElement('div');

                const user = {
                    name: 'John Doe',
                    id: 123,
                    email: 'john.doe@example.com'
                };

                await getUserView(mainContent, user);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('User', 'Header should be "User"');
                const listItems = content.querySelectorAll('li');
                chai.expect(listItems.length).to.equal(4, 'There should be 4 list items');
                chai.expect(listItems[0].textContent).to.equal('Name: John Doe', 'First item should be "Name: John Doe"');
                chai.expect(listItems[1].textContent).to.equal('Id: 123', 'Second item should be "Id: 123"');
                chai.expect(listItems[2].textContent).to.equal('Owner: john.doe@example.com', 'Third item should be "Owner: john.doe@example.com"');
                const link = listItems[3].querySelector('a');
                chai.expect(link.textContent).to.equal('Rentals', 'Fourth item should contain a link with text "Rentals"');
                chai.expect(link.getAttribute('href')).to.equal('#rentals/users/123', 'Link should have correct href');
            } else {
                this.skip();
            }
        });

        it('should render empty user rentals view correctly', async function() {
            if (typeof getUserRentalsEmpty === 'function') {
                const mainContent = document.createElement('div');

                await getUserRentalsEmpty(mainContent);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('User Rentals', 'Header should be "User Rentals"');
                chai.expect(content.querySelector('p').textContent).to.equal('No rentals available.', 'Paragraph should indicate no rentals');
                const link = content.querySelector('a');
                chai.expect(link.textContent).to.equal('Create a Rental', 'Link should be "Create a Rental"');
                chai.expect(link.getAttribute('href')).to.equal('#rentals/create', 'Link should have correct href');
            } else {
                this.skip();
            }
        });

        it('renders user rentals view correctly', async () => {
            const mainContent = document.createElement('div');
            const enrichedRentals = [
                { id: 1, user: 'John Doe', court: 'Court A', club: 'Test Club A', date: '2023-10-01', startDuration: 2, endDuration: 4 },
                { id: 2, user: 'Jane Doe', court: 'Court B', club: 'Test Club B', date: '2023-10-02', startDuration: 6, endDuration: 8 }
            ];
            const skip = 0, limit = 2, next = 2, previous = -1, userId = '123';

            await getUserRentalsView(mainContent, enrichedRentals, skip, limit, next, previous, userId);

            chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
            const content = mainContent.firstChild;

            chai.expect(content).to.not.be.null;
            chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
            chai.expect(content.querySelector('h1').textContent).to.equal('User Rentals', 'Header should be "User Rentals"');

            const rows = content.querySelectorAll('div[style*="display: flex"]');
            chai.expect(rows.length).to.equal(enrichedRentals.length + 1, 'There should be correct number of rows (including header)');

            const headerRow = rows[0];
            chai.expect(headerRow.children.length).to.equal(9, 'Header row should have 9 columns');

            const dataRows = Array.from(rows).slice(1);
            dataRows.forEach((row, index) => {
                chai.expect(row.children.length).to.equal(9, `Row ${index + 1} should have 9 columns`);
                const cells = row.children;
                chai.expect(cells[0].textContent).to.equal(String(enrichedRentals[index].id), `Row ${index + 1} ID should match`);
                chai.expect(cells[1].textContent).to.equal(enrichedRentals[index].user, `Row ${index + 1} User should match`);
                chai.expect(cells[2].textContent).to.equal(enrichedRentals[index].court, `Row ${index + 1} Court should match`);
                chai.expect(cells[3].textContent).to.equal(enrichedRentals[index].club, `Row ${index + 1} Club should match`);
                chai.expect(cells[4].textContent).to.equal(enrichedRentals[index].date, `Row ${index + 1} Date should match`);
                chai.expect(Number(cells[5].textContent)).to.equal(enrichedRentals[index].startDuration, `Row ${index + 1} start duration should match`);
                chai.expect(Number(cells[6].textContent)).to.equal(enrichedRentals[index].endDuration, `Row ${index + 1} end duration should match`);
            });
        });
    });

    describe('Testing Club Views', () => {
        it('should render club list view correctly', async function () {
            if (typeof getClubsView === 'function') {
                const mainContent = document.createElement('div');

                const clubs = [
                    {id: '1', name: 'Club A'},
                    {id: '2', name: 'Club B'}
                ];
                const previous = 0, next = 2, limit = 2;

                await getClubsView(mainContent, clubs, previous, next, limit);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Clubs', 'Header should be "Clubs"');
                const rows = content.querySelectorAll('div[style*="display: flex"]');
                chai.expect(rows.length).to.equal(clubs.length + 1, 'There should be correct number of rows (including header)');
            } else {
                this.skip();
            }
        });

        it('should render clubs by name view correctly', async function() {
            if (typeof getClubsByNameView === 'function') {
                const mainContent = document.createElement('div');

                const clubs = [
                    { id: '1', name: 'Club A' },
                    { id: '2', name: 'Club B' }
                ];

                await getClubsByNameView(mainContent, clubs);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Clubs', 'Header should be "Clubs"');
                const listItems = content.querySelectorAll('li');
                chai.expect(listItems.length).to.equal(clubs.length, 'There should be correct number of list items');
                listItems.forEach((item, index) => {
                    const link = item.querySelector('a');
                    chai.expect(link.textContent).to.equal(`Club: ${clubs[index].name}`, `List item ${index + 1} should match club name`);
                    chai.expect(link.getAttribute('href')).to.equal(`#clubs/${clubs[index].id}`, `List item ${index + 1} should have correct href`);
                });
            } else {
                this.skip();
            }
        });

        it('should render club details view correctly', async function () {
            const mainContent = document.createElement('div');

            const user = {
                name: 'John Doe',
                id: 123,
                email: 'john.doe@example.com'
            };

            const club = {
                id: '1',
                name: 'Test Club A',
                owner: '123',
            };

            await getClubView(mainContent, club, user);
            
            chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
            const content = mainContent.firstChild;

            chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
            chai.expect(content.querySelector('h1').textContent).to.equal('Club', 'Header should be "Club"');
            const listItems = content.querySelectorAll('li');
            chai.expect(listItems.length).to.equal(4, 'There should be 4 list items');
            chai.expect(listItems[0].textContent).to.equal('Name: Test Club A', 'First item should be "Name: Test Club A"');
            chai.expect(listItems[1].textContent).to.equal('Number: 1', 'Second item should be "Number: 1"');
            const ownerLink = listItems[2].querySelector('a');
            chai.expect(ownerLink.textContent).to.equal('Owner: John Doe', 'Third item should contain a link with text "Owner: John Doe"');
            chai.expect(ownerLink.getAttribute('href')).to.equal('#users/123', 'Owner link should have correct href');
            const courtsLink = listItems[3].querySelector('a');
            chai.expect(courtsLink.textContent).to.equal('Courts', 'Fourth item should contain a link with text "Courts"');
            chai.expect(courtsLink.getAttribute('href')).to.equal('#courts/clubs/1', 'Courts link should have correct href');
        });

        it('should render create club view elements correctly', async function () {
            if (typeof createClubView === 'function') {
                const mainContent = document.createElement('div');

                await createClubView(mainContent);

                // Verify min-width style
                chai.expect(mainContent.style.minWidth).to.equal('100px', 'min-width should be 100px');

                // Verify info paragraph
                const infoParagraph = document.querySelector('p.anim');
                chai.expect(infoParagraph).to.not.be.null;
                chai.expect(infoParagraph.textContent).to.equal(
                    'Welcome to our page Chelas Padel. Here you can create a new club just put the club name to create.',
                    'Info paragraph should have the correct text'
                );

                // Verify form structure
                const clubForm = mainContent.querySelector('form');
                chai.expect(clubForm).to.not.be.null;

                // Verify label
                const label = clubForm.querySelector('label[for="clubNameId"]');
                chai.expect(label).to.not.be.null;
                chai.expect(label.textContent).to.equal('Club Name', 'Label should be "Club Name"');

                // Verify input field
                const inputField = clubForm.querySelector('#clubNameId');
                chai.expect(inputField).to.not.be.null;
                chai.expect(inputField.type).to.equal('text', 'Input field should be of type "text"');

                // Verify submit button
                const submitButton = clubForm.querySelector('input[type="submit"]');
                chai.expect(submitButton).to.not.be.null;
                chai.expect(submitButton.value).to.equal('Create Club', 'Submit button should have the correct value');
            } else {
                this.skip();
            }
        });
    });

    describe('Testing Court Views', () => {
        it('should render court list view correctly', async function () {
            if (typeof courtsListView === 'function') {
                const mainContent = document.createElement('div');

                const courts = [
                    {id: '1', name: 'Court A', club: 'Club A'},
                    {id: '2', name: 'Court B', club: 'Club B'}
                ];
                const clubId = '1', skip = 0, previous = 0, next = 2, limit = 2;

                await courtsListView(mainContent, courts, clubId, skip, limit, previous, next);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Courts of club: 1', 'Header should be "Courts of Club: 1"');
                const rows = content.querySelectorAll('div[style*="display: flex"]');
                chai.expect(rows.length).to.equal(courts.length + 1, 'There should be correct number of rows (including header)');
            } else {
                this.skip();
            }
        });

        it('should render empty court rental by date view correctly', async function() {
            if (typeof emptyCourtRentalsByDateView === 'function') {
                const mainContent = document.createElement('div');

                await emptyCourtRentalsByDateView(mainContent);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Court Rentals', 'Header should be "Court Rentals"');
                chai.expect(content.querySelector('p').textContent).to.equal('No rentals available for this date.', 'Paragraph should indicate no rentals');
            } else {
                this.skip();
            }
        });

        it('should render empty court rentals view correctly', async function() {
            if (typeof emptyCourtRentalsByDateView === 'function') {
                const mainContent = document.createElement('div');

                await emptyCourtRentalsView(mainContent);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Court Rentals', 'Header should be "Court Rentals"');
                chai.expect(content.querySelector('p').textContent).to.equal('No rentals available.', 'Paragraph should indicate no rentals');
            } else {
                this.skip();
            }
        });

        it('should render empty courts list view correctly', async function(){
            if (typeof emptyCourtsListView === 'function') {
                const mainContent = document.createElement('div');
                const clubId = '1';

                await emptyCourtsListView(mainContent, clubId);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Courts', 'Header should be "Courts"');
                chai.expect(content.querySelector('p').textContent).to.equal('No courts available.', 'Paragraph should indicate no courts');
            } else {
                    this.skip();
            }
        });

        it('should render get Available Hours View correctly', async function() {
            if (typeof getAvailableHoursView === 'function') {
                const mainContent = document.createElement('div');
                const club = {
                    id: '1',
                    name: 'Test Club A',
                    owner: '123',
                };
                const court = {
                    id: '1',
                    name: 'Court A',
                    club: club.id,
                };
                const availableHours = '8, 9, 10, 11, 12';

                await getAvailableHoursView(mainContent, club, court, availableHours);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('UL', 'Content should be a UL');
                const items = content.querySelectorAll('li');
                chai.expect(items[0].textContent).to.equal('Club: Test Club A');
                chai.expect(items[1].textContent).to.equal('Court Details:');
                chai.expect(items[2].textContent).to.equal('Name: Court A');
                chai.expect(items[3].textContent).to.equal('Available Hours : 8, 9, 10, 11, 12');
                chai.expect(content.querySelector('button')).to.not.be.null;
            } else {
                this.skip();
            }
        });

        it('should render get court rentals view correctly', async function() {
            if (typeof getCourtRentalsView === 'function') {
                const mainContent = document.createElement('div');
                const enrichedRentals = [
                    { id: '1', court: 'Court A' },
                    { id: '2', court: 'Court B' }
                ];
                const clubId = '1';
                const courtId = '1';
                const limit = 2;
                const previous = 0;
                const next = 2;

                await getCourtRentalsView(mainContent, enrichedRentals, clubId, courtId, limit, previous, next);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Court Rentals', 'Header should be "Court Rentals"');
                const createLink = content.querySelector('a');
                chai.expect(createLink).to.not.be.null;
                chai.expect(createLink.textContent).to.equal('Create a Rental', 'Link should be "Create a Rental"');
                chai.expect(createLink.getAttribute('href')).to.equal('#rentals/create', 'Link should have correct href');
            } else {
                this.skip();
            }
        });

        it('should render get court rentals by date view correctly', async function() {
            if (typeof getCourtsRentalsByDateView) {
                const mainContent = document.createElement('div');
                const enrichedRentals = [
                    { id: '1', court: 'Court A', date: '2025-10-01' },
                    { id: '2', court: 'Court B', date: '2025-10-02' }
                ];
                const clubId = '1';
                const courtId = '1';
                const date = '2025-10-01';
                const limit = 2;
                const skip = 0;

                await getCourtsRentalsByDateView(mainContent, enrichedRentals, clubId, courtId, date, limit, skip);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Court Rentals', 'Header should be "Court Rentals"');
            } else {
                this.skip();
            }
        });

        it('should render get court view correctly', async function() {
            if (typeof getCourtView) {
                const mainContent = document.createElement('div');
                const club = {
                    id: '1',
                    name: 'Test Club A',
                    owner: '123',
                };
                const court = {
                    id: '1',
                    name: 'Court A',
                    club: club.id,
                };

                await getCourtView(mainContent, club, court);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Court Details', 'Header should be "Court Details"');
                const listItems = content.querySelectorAll('li');
                chai.expect(listItems.length).to.equal(4, 'There should be 4 list items');
                chai.expect(listItems[0].textContent).to.equal('Id: 1', 'First item should be "Id: 1"');
                chai.expect(listItems[1].textContent).to.equal('Name: Court A', 'First item should be "Name: Court A"');
                chai.expect(listItems[2].textContent).to.equal('Club: Test Club A', 'Third item should be "Club: Test Club A"');
            } else {
                this.skip();
            }
        });

        it('should render create court view correctly', async function () {
            if (typeof createCourtView === 'function') {
                const mainContent = document.createElement('div');
                const clubId = '123';

                await createCourtView(mainContent, clubId);

                const courtForm = mainContent.querySelector('form');
                chai.expect(courtForm).to.not.be.null;

                const label = courtForm.querySelector('label[for="courtNameId"]');
                chai.expect(label).to.not.be.null;
                chai.expect(label.textContent).to.equal('Court Name', 'Label should be "Court Name"');

                const inputField = courtForm.querySelector('#courtNameId');
                chai.expect(inputField).to.not.be.null;
                chai.expect(inputField.type).to.equal('text', 'Input field should be of type "text"');

                const submitButton = courtForm.querySelector('input[type="submit"]');
                chai.expect(submitButton).to.not.be.null;
                chai.expect(submitButton.value).to.equal('Create Court', 'Submit button should have the correct value');
            } else {
                this.skip();
            }
        });
    });

    describe('Testing Rental Views', () => {
        it('should render create rental view correctly', async function () {
            if (typeof createRentalView) {
                const mainContent = document.createElement('div');
                const clubs = [
                    {id: '1', name: 'Club A', owner: '123'},
                    {id: '2', name: 'Club B', owner: '123'}
                ];
                const courts = {
                    '1': [{id: '1', name: 'Court A'}],
                    '2': [{id: '2', name: 'Court B'}]
                };

                const user = {id: '123', name: 'John Doe', email: 'john.doe@example.com'};

                await createRentalView(mainContent, clubs, courts);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;
                chai.expect(content.tagName).to.equal('FORM', 'Content should be a FORM');

                const clubSelect = content.querySelector('#clubSelect');
                chai.expect(clubSelect).to.not.be.null;
                chai.expect(clubSelect.tagName).to.equal('SELECT', 'Club select should be a SELECT element');

                const courtSelect = content.querySelector('#courtSelect');
                chai.expect(courtSelect).to.not.be.null;
                chai.expect(courtSelect.tagName).to.equal('SELECT', 'Court select should be a SELECT element');

                const rentalDate = content.querySelector('#rentalDate');
                chai.expect(rentalDate).to.not.be.null;
                chai.expect(rentalDate.tagName).to.equal('INPUT', 'Rental date should be an INPUT element');
                chai.expect(rentalDate.type).to.equal('date', 'Rental date input should be of type "date"');
                chai.expect(rentalDate.value).to.equal('2025-06-15', 'Rental date should have the correct default value');
                chai.expect(rentalDate.min).to.equal('2025-06-15', 'Rental date should have the correct minimum value');

                const startDuration = content.querySelector('#startDuration');
                chai.expect(startDuration).to.not.be.null;
                chai.expect(startDuration.tagName).to.equal('INPUT', 'Start duration should be an INPUT element');
                chai.expect(startDuration.type).to.equal('number', 'Start duration input should be of type "number"');
                chai.expect(Number(startDuration.value)).to.equal(0, 'Start duration should have the correct default value');
                chai.expect(Number(startDuration.min)).to.equal(0, 'Start duration should have the correct minimum value');
                chai.expect(Number(startDuration.max)).to.equal(22, 'Start duration should have the correct maximum value');

                const endDuration = content.querySelector('#endDuration');
                chai.expect(endDuration).to.not.be.null;
                chai.expect(endDuration.tagName).to.equal('INPUT', 'End duration should be an INPUT element');
                chai.expect(endDuration.type).to.equal('number', 'End duration input should be of type "number"');
                chai.expect(Number(endDuration.value)).to.equal(1, 'End duration should have the correct default value');
                chai.expect(Number(endDuration.min)).to.equal(1, 'End duration should have the correct minimum value');
                chai.expect(Number(endDuration.max)).to.equal(23, 'End duration should have the correct maximum value');

                const submitButton = content.querySelector('input[type="submit"]');
                chai.expect(submitButton).to.not.be.null;
                chai.expect(submitButton.value).to.equal('Create Rental', 'Submit button should have the correct value');
            } else {
                this.skip();
            }
        });


        it('should render rental empty view correctly', async function () {
            if (typeof getRentalEmptyView === 'function') {
                const mainContent = document.createElement('div');

                await getRentalEmptyView(mainContent);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Rental was deleted or does not exist', 'Header should be "Rental was deleted or does not exist"');
                const link = content.querySelector('a');
                chai.expect(link.textContent).to.equal('Create a Rental', 'Link should be "Create a Rental"');
                chai.expect(link.getAttribute('href')).to.equal('#rentals/create', 'Link should have correct href');
                const button = content.querySelector('button');
                chai.expect(button).to.not.be.null;
                chai.expect(button.textContent).to.equal('Go Back', 'Button should display "Go Back"');
            } else {
                this.skip();
            }
        });

        it('should render rental view correctly', async function () {
            if (typeof getRentalView === 'function') {
                const mainContent = document.createElement('div');

                const rental = {
                    id: '1',
                    date: '2025-06-15',
                    startDuration: 10,
                    endDuration: 12,
                    user: '123',
                    court: '456',
                    club: '789'
                };

                const user = {
                    id: '123',
                    name: 'John Doe'
                };

                const court = {
                    id: '456',
                    name: 'Court A'
                };

                const club = {
                    id: '789',
                    name: 'Club A'
                };

                await getRentalView(mainContent, rental, user, court, club);

                chai.expect(mainContent.children.length).to.equal(1, 'Content should be replaced');
                const content = mainContent.firstChild;

                chai.expect(content.tagName).to.equal('DIV', 'Content should be a DIV');
                chai.expect(content.querySelector('h1').textContent).to.equal('Rental Details', 'Header should be "Rental Details"');

                const listItems = content.querySelectorAll('li');
                chai.expect(listItems.length).to.equal(8, 'There should be 8 list items');
                chai.expect(listItems[0].textContent).to.equal('Id: 1', 'First item should be "Id: 1"');
                chai.expect(listItems[1].textContent).to.equal('Date: 2025-06-15', 'Second item should be "Date: 2025-06-15"');
                chai.expect(listItems[2].textContent).to.equal('StartDuration: 10', 'Third item should be "StartDuration: 10"');
                chai.expect(listItems[3].textContent).to.equal('EndDuration: 12', 'Fourth item should be "EndDuration: 12"');

                const userLink = listItems[4].querySelector('a');
                chai.expect(userLink.textContent).to.equal('User: John Doe', 'Fifth item should contain a link with text "User: John Doe"');
                chai.expect(userLink.getAttribute('href')).to.equal('#users/123', 'User link should have correct href');

                const courtLink = listItems[5].querySelector('a');
                chai.expect(courtLink.textContent).to.equal('Court: Court A', 'Sixth item should contain a link with text "Court: Court A"');
                chai.expect(courtLink.getAttribute('href')).to.equal('#courts/456', 'Court link should have correct href');

                chai.expect(listItems[6].textContent).to.equal('Club: Club A', 'Seventh item should be "Club: Club A"');

                const buttons = listItems[7].querySelectorAll('button');
                chai.expect(buttons.length).to.equal(2, 'Eighth item should contain two buttons');
                chai.expect(buttons[0].textContent).to.equal('Delete', 'First button should display "Delete"');
                chai.expect(buttons[1].textContent).to.equal('Update', 'Second button should display "Update"');
            } else {
                this.skip();
            }
        });

        it('should render delete rental view elements correctly', async function () {
            if (typeof deleteRentalView === 'function') {
                const mainContent = document.createElement('div');
                const rentalId = '123';

                await deleteRentalView(mainContent, rentalId);

                chai.expect(mainContent.style.minWidth).to.equal('1000px', 'min-width should be 1000px');

                const title = mainContent.querySelector('h1');
                chai.expect(title).to.not.be.null;
                chai.expect(title.textContent).to.equal('Do you really want to delete this rental?', 'Title should be correct');

                const yesButton = mainContent.querySelector('button:nth-child(2)');
                chai.expect(yesButton).to.not.be.null;
                chai.expect(yesButton.textContent).to.equal('Yes', 'Yes button should be present');

                const noButton = mainContent.querySelector('button:nth-child(3)');
                chai.expect(noButton).to.not.be.null;
                chai.expect(noButton.textContent).to.equal('No', 'No button should be present');
            } else {
                this.skip();
            }
        });

        it('should render update rental view elements correctly', async function () {
            if (typeof updateRentalView === 'function') {
                const mainContent = document.createElement('div');
                const rental = {
                    date: '2025-06-15',
                    startDuration: 10,
                    endDuration: 12,
                    court: 'Court A',
                    club: 'Club A'
                };
                const rentalId = '123';

                await updateRentalView(mainContent, rental, rentalId);

                const info = mainContent.querySelector('p');
                chai.expect(info).to.not.be.null;
                chai.expect(info.textContent).to.equal('Select the options to update rental', 'Info paragraph should be correct');

                const rentalForm = mainContent.querySelector('form');
                chai.expect(rentalForm).to.not.be.null;

                const dateInput = rentalForm.querySelector('#date');
                chai.expect(dateInput).to.not.be.null;
                chai.expect(dateInput.type).to.equal('date', 'Date input should be of type "date"');
                chai.expect(dateInput.value).to.equal(rental.date, 'Date input should have the correct value');

                const startDurationInput = rentalForm.querySelector('#startDuration');
                chai.expect(startDurationInput).to.not.be.null;
                chai.expect(startDurationInput.type).to.equal('number', 'Start duration input should be of type "number"');
                chai.expect(startDurationInput.value).to.equal(String(rental.startDuration), 'Start duration input should have the correct value');

                const endDurationInput = rentalForm.querySelector('#endDuration');
                chai.expect(endDurationInput).to.not.be.null;
                chai.expect(endDurationInput.type).to.equal('number', 'End duration input should be of type "number"');
                chai.expect(endDurationInput.value).to.equal(String(rental.endDuration), 'End duration input should have the correct value');

                const buttonGroup = rentalForm.querySelector('div');
                chai.expect(buttonGroup).to.not.be.null;
                chai.expect(buttonGroup.style.display).to.equal('flex', 'Button group should have display set to "flex"');
                chai.expect(buttonGroup.style.gap).to.equal('10px', 'Button group should have gap set to "10px"');

                const yesButton = buttonGroup.querySelector('input[type="submit"]');
                chai.expect(yesButton).to.not.be.null;
                chai.expect(yesButton.value).to.equal('Yes', 'Yes button should have the correct value');

                const noButton = buttonGroup.querySelector('button');
                chai.expect(noButton).to.not.be.null;
                chai.expect(noButton.textContent).to.equal('No', 'No button should have the correct text');
            } else {
                this.skip();
            }
        });
    });

    describe('Testing Account Views', () => {
        it('should render create account view correctly', async function () {
            if (typeof createAccount === 'function') {
                const mainContent = document.createElement('div');

                await createAccount(mainContent);

                chai.expect(mainContent.children.length).to.equal(3, 'Content should be replaced');
                const content = mainContent;

                chai.expect(content.firstChild.tagName).to.equal('H1', 'Content should be a H1');
                chai.expect(content.querySelector('h1').textContent).to.equal('Create Account', 'Header should be "Create Account"');

                const form = content.querySelector('form');
                chai.expect(form).to.not.be.null;

                const nameInput = content.querySelector('#Name');
                chai.expect(nameInput).to.not.be.null;
                chai.expect(nameInput.type).to.equal('text', 'Name input should be of type "text"');

                const emailInput = content.querySelector('#Email');
                chai.expect(emailInput).to.not.be.null;
                chai.expect(emailInput.type).to.equal('text', 'Email input should be of type "text"');

                const passwordInput = content.querySelector('#Password');
                chai.expect(passwordInput).to.not.be.null;
                chai.expect(passwordInput.type).to.equal('password', 'Password input should be of type "password"');

                const submitButton = content.querySelector('input[type="submit"]');
                chai.expect(submitButton).to.not.be.null;
                chai.expect(submitButton.value).to.equal('Create', 'Submit button should have the correct value');
            } else {
                this.skip();
            }
        });

        it('should render login view correctly when not logged in', async function () {
            if (typeof createLoginLogout === 'function') {
                const mainContent = document.createElement('div');
                window.token = null; // Simulate not being logged in

                await createLoginLogout(mainContent);

                chai.expect(mainContent.children.length).to.equal(3, 'Content should include title and buttons');
                const title = mainContent.querySelector('h1');
                chai.expect(title.textContent).to.equal('Login', 'Header should be "Login"');

                const loginForm = mainContent.querySelector('form');
                chai.expect(loginForm).to.not.be.null;
                chai.expect(loginForm.querySelector('#Name')).to.not.be.null;
                chai.expect(loginForm.querySelector('#Password')).to.not.be.null;

                const createAccountLink = mainContent.querySelector('a[href="#create"]');
                chai.expect(createAccountLink).to.not.be.null;
                chai.expect(createAccountLink.textContent).to.equal('Create an Account', 'Link should be "Create an Account"');
            } else {
                this.skip();
            }
        });

        it('should render logout view correctly when logged in', async function () {
            if (typeof createLoginLogout === 'function') {
                const mainContent = document.createElement('div');
                window.token = 'mockToken';

                await createLoginLogout(mainContent);

                chai.expect(mainContent.children.length).to.equal(3, 'Content should include title and buttons');
                const title = mainContent.querySelector('h1');
                chai.expect(title.textContent).to.equal('Are you sure you want to logout?', 'Header should be "Are you sure you want to logout?"');

                const buttons = mainContent.querySelectorAll('button');
                chai.expect(buttons.length).to.equal(2, 'There should be 2 buttons');
                chai.expect(buttons[0].textContent).to.equal('Logout', 'First button should display "Logout"');
                chai.expect(buttons[1].textContent).to.equal('Continue Logged In', 'Second button should display "Continue Logged In"');

                buttons.forEach((button) => {
                    chai.expect(button.style.marginLeft).to.equal('5px', 'Button should have margin-left of 5px');
                    chai.expect(button.style.marginTop).to.equal('10px', 'Button should have margin-top of 10px');
                });
            } else {
                this.skip();
            }
        });

        it('should render not logged in view elements correctly', async function () {
            if (typeof notLoggedIn === 'function') {
                const mainContent = document.createElement('div');
                window.token = null;

                await notLoggedIn(mainContent);

                const title = mainContent.querySelector('h1');
                chai.expect(title).to.not.be.null;
                chai.expect(title.textContent).to.equal('You must login if you want to make changes', 'Title should be correct');

                const buttons = mainContent.querySelectorAll('button');
                chai.expect(buttons.length).to.equal(3, 'There should be 3 buttons');

                const goBackButton = buttons[0];
                chai.expect(goBackButton.textContent).to.equal('I dont want to make changes right now', 'Go Back button should be present');

                const createAccountButton = buttons[1];
                chai.expect(createAccountButton.textContent).to.equal('Create an Account', 'Create Account button should be present');

                const loginButton = buttons[2];
                chai.expect(loginButton.textContent).to.equal('Login', 'Login button should be present');
            } else {
                this.skip();
            }
        });
    });
})

evaluateTests();