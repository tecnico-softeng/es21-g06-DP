describe('Manage Item Combination In Questions Walk-through', () => {
  function validateQuestion(title, content, item, comb) {
    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within(() => {
        cy.get('.headline').should('contain', title);
        cy.get('span > p').should('contain', content);
        cy.get('span > p').should('contain', item);
        cy.get('span > p').should('contain', comb);
      });
  }

  function validateQuestionFull(title, content, item, comb) {
    cy.log('Validate question with show dialog.');

    cy.get('[data-cy="questionTitleGrid"]').first().click();

    validateQuestion(title, content, item, comb);

    cy.get('button').contains('close').scrollIntoView();
    cy.get('button').contains('close').should('be.visible').click();

    Cypress.on('uncaught:exception', (err) => {
      console.log(err);
      // returning false here prevents Cypress from
      // failing the test
      return false;
    });
  }
  
  before(() => {
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
  });
  after(() => {
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
  });

  beforeEach(() => {
    cy.demoTeacherLogin();
    cy.route('GET', '/courses/*/questions').as('getQuestions');
    cy.route('GET', '/courses/*/topics').as('getTopics');
    cy.get('[data-cy="managementMenuButton"]').click();
    cy.get('[data-cy="questionsTeacherMenuButton"]').click();

    cy.wait('@getQuestions').its('status').should('eq', 200);

    cy.wait('@getTopics').its('status').should('eq', 200);
  });
  afterEach(() => {
    cy.logout();
  });


  it('Creates a new item combination in question', function () {
    cy.get('button')
      .contains('New Question')
      .click();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get('[data-cy="questionTypeInput"]')
    .type('item_combination', { force: true })
    .click({ force: true });

    cy.wait(1000);

    cy.get(
      '[data-cy="questionTitleTextArea"]'
    ).type('Cypress Question Example - 01', { force: true });
    cy.get(
      '[data-cy="questionQuestionTextArea"]'
    ).type('Cypress Question Example - Content - 01', { force: true });

    cy.get(
      '[data-cy="itemGroupAddItemNameArea"]'
    ).first().type('Item 1 from Group 1', { force: true });

    cy.get(`[data-cy="AddItemButton"]`).first().click({ force: true });

    cy.get(
      '[data-cy="itemGroupAddItemNameArea"]'
    ).last().scrollIntoView();

    cy.get(
      '[data-cy="itemGroupAddItemNameArea"]'
    ).last().type('Item 1 from Group 2', { force: true });

    cy.get(`[data-cy="AddItemButton"]`).last().click({ force: true });

    cy.get(`[data-cy="SelectCombinationOne"]`).scrollIntoView();

    cy.get(`[data-cy="SelectCombinationOne"]`).click({ force: true });

    cy.get(".v-menu__content").filter(':visible').contains(`1,`).click({ force: true });

    cy.get(`[data-cy="SelectCombinationTwo"]`).last().click({ force: true });

    cy.get(".v-menu__content").filter(':visible').contains(`1,`).click({ force: true });

    cy.get(`[data-cy="AddCombination"]`).click({ force: true });

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button').contains('Save').click();
  
    cy.wait('@postQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01');

    validateQuestionFull(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01',
      'Item 1 from Group 1',
      '✔ Item 1');
  });


  it('Can view question (with button)', function () {
    cy.get('tbody tr')
      .first()
      .within(() => {
        cy.get('button').contains('visibility').click();
      });

    cy.wait(1000);

    validateQuestion(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01',
      'Item 1 from Group 1',
      '✔ Item 1'
    );

    cy.get('button').contains('close').click();
  });


  it('Can view question (with click)', function () {
    cy.get('[data-cy="questionTitleGrid"]').first().click();

    cy.wait(1000); //making sure codemirror loaded

    validateQuestion(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01',
      'Item 1 from Group 1',
      '✔ Item 1'
    );

    cy.get('button').contains('close').click();
  });


  it('Can update title (with right-click)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('[data-cy="questionTitleGrid"]').first().rightclick();

    cy.wait(1000); //making sure codemirror loaded

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(() => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionTitleTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - 01 - Edited', { force: true });

        cy.get('button').contains('Save').click();
      });

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited');

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress Question Example - Content - 01',
      'Item 1 from Group 1',
      '✔ Item 1');
  });


  it('Can update content (with button)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within(() => {
        cy.get('button').contains('edit').click();
      });

    cy.wait(1000); //making sure codemirror loaded

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within(() => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionQuestionTextArea"]')
          .clear({ force: true })
          .type('Cypress New Content For Question!', { force: true });
      });
    cy.get(
      '[data-cy="itemGroupAddItemNameArea"]'
    ).first().type('Item 2 from Group 1', { force: true });

    cy.get(`[data-cy="AddItemButton"]`).first().click({ force: true });

    cy.get(`[data-cy="SelectCombinationOne"]`).scrollIntoView();

    cy.get(`[data-cy="SelectCombinationOne"]`).click({ force: true });

    cy.get('.v-menu__content').filter(':visible').contains(`2,`).click({ force: true });

    cy.get(`[data-cy="SelectCombinationTwo"]`).last().click({ force: true });

    cy.get('.v-menu__content').filter(':visible').contains(`1,`).click({ force: true });

    cy.get(`[data-cy="AddCombination"]`).click({ force: true });

    cy.get('button').contains('Save').click();

    cy.wait('@updateQuestion').its('status').should('eq', 200);

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress New Content For Question!',
      'Item 2 from Group 1',
      '✔ Item 1'
    );
  });

  it('Can duplicate question', function () {
    cy.get('tbody tr')
      .first()
      .within(() => {
        cy.get('button').contains('cached').click();
      });

    cy.wait(1000); //making sure codemirror loaded

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get('[data-cy="questionTitleTextArea"]')
      .should('have.value', 'Cypress Question Example - 01 - Edited')
      .type('{end} - DUP', { force: true });
    cy.get('[data-cy="questionQuestionTextArea"]').should(
      'have.value',
      'Cypress New Content For Question!'
    );

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.wait(1000);

    cy.get('button').contains('Save').click();

    cy.wait('@postQuestion').its('status').should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited - DUP');

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited - DUP',
      'Cypress New Content For Question!',
      'Item 1 from Group 1',
      '✔ Item 1'
    );
  });

  it('Can delete created question', function () {
    cy.route('DELETE', '/questions/*').as('deleteQuestion');
    cy.get('tbody tr')
      .first()
      .within(() => {
        cy.get('button').contains('delete').click();
      });

    cy.wait('@deleteQuestion').its('status').should('eq', 200);
  });

});
