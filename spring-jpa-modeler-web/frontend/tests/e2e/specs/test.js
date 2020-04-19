// https://docs.cypress.io/api/introduction/api.html

describe('Login', () => {
  it('Opens the login Page', () => {
    cy.visit('/login');
    cy.contains('h1', 'Login');
  });
});
