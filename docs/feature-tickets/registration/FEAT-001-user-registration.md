# FEAT-001 — User registration

| Field | Value |
| --- | --- |
| Issue type | Story |
| Status | Ready for development |
| Priority | High |
| Reporter | Business analyst |
| Assignee | Unassigned |
| Components | UI, API, Database |
| Labels | `registration` `account` `public` |
| Fix version | Unscheduled |
| Related | [FEAT-002](../activation/FEAT-002-account-activation.md) — opening the activation link |

## Summary

A visitor can create an account with a username, email, and password. The account is stored inactive, assigned the user role, and an activation email is sent. The visitor cannot sign in until the account is activated.

## User story

As a visitor without an account, I want to register with a username, email, and password, so that I can receive an activation email and later sign in.

## Background

Registration is a public flow. It is reached from the home page (“Register a new account”) and from the account menu. It does not require a signed-in session.

This story covers creating the account only. Opening the activation link and turning the account on is a separate story.

## Scope

- Registration page with client-side validation and a password strength indicator.
- Public `POST /api/register` that creates the account and sends the activation email.
- A persisted user row: login and email stored in lowercase, password stored as a hash, account inactive, activation key set, `ROLE_USER` assigned.

## Out of scope

- Activating the account from the email link.
- Signing in, password reset, and account settings.
- Admin creation of users.
- Choosing a language. The client sends English (`en`).

## Business rules

1. Username, email, password, and password confirmation are required.
2. Username is 1–50 characters and matches the allowed login pattern (letters, digits, and `_.@-`, or an email-shaped login).
3. Email is 5–254 characters and must be a valid email address.
4. Password is at least 4 characters. The form rejects more than 50 characters. The API rejects more than 100 characters.
5. Password and confirmation must match before the form is submitted.
6. Login and email are stored in lowercase. `Admin` and `admin` are the same login.
7. The password is never stored or returned in clear text. Only a hash is saved.
8. A new account is inactive (`activated = false`) and receives a single-use activation key.
9. A new account is granted the `ROLE_USER` authority only.
10. If the login or email already belongs to an **activated** account, registration is rejected.
11. If the login or email belongs only to an **inactive** account, that inactive account is removed and the new registration replaces it.
12. On success the API responds `201 Created` with an empty body and sends an activation email to the address provided.

## Acceptance criteria

```gherkin
Feature: User registration

  Background:
    Given the visitor is not signed in
    And the visitor is on the registration page

  Scenario: Register a new account
    Given no account exists for username "jane.doe" or email "jane.doe@example.com"
    When the visitor enters username "jane.doe"
    And the visitor enters email "jane.doe@example.com"
    And the visitor enters password "secret1"
    And the visitor enters the same password as confirmation
    And the visitor submits the registration form
    Then the registration succeeds
    And a success message is shown
    And an inactive user is stored with login "jane.doe"
    And the stored email is "jane.doe@example.com"
    And the stored password is a hash, not "secret1"
    And the user has authority "ROLE_USER"
    And an activation key is stored
    And an activation email is sent to "jane.doe@example.com"

  Scenario: Login and email are stored in lowercase
    When the visitor registers with username "Jane.Doe" and email "Jane.Doe@Example.com"
    Then the stored login is "jane.doe"
    And the stored email is "jane.doe@example.com"

  Scenario: Reject a duplicate login on an activated account
    Given an activated account already uses login "jane.doe"
    When the visitor submits a registration with username "jane.doe"
    Then the registration is rejected
    And no second account is created
    And no activation email is sent

  Scenario: Reject a duplicate email on an activated account
    Given an activated account already uses email "jane.doe@example.com"
    When the visitor submits a registration with email "jane.doe@example.com" and a new username
    Then the registration is rejected
    And no second account is created
    And no activation email is sent

  Scenario: Replace an inactive account that uses the same login or email
    Given an inactive account already uses login "jane.doe" or email "jane.doe@example.com"
    When the visitor successfully registers with that login or email
    Then the previous inactive account is removed
    And one new inactive account is stored
    And an activation email is sent for the new account

  Scenario: Block submit when required fields are missing
    When the visitor submits the form with username, email, password, or confirmation empty
    Then the form is not submitted
    And the visitor sees that the empty field is required

  Scenario: Block submit when the username is invalid
    When the visitor enters a username that is empty, longer than 50 characters, or outside the allowed pattern
    Then the form is not submitted
    And the visitor sees that the username is invalid or too long

  Scenario: Block submit when the email is invalid
    When the visitor enters an email shorter than 5 characters, longer than 254 characters, or not a valid email
    Then the form is not submitted
    And the visitor sees that the email is invalid

  Scenario: Block submit when the password is too short or the confirmation does not match
    When the visitor enters a password shorter than 4 characters
    Or the confirmation does not match the password
    Then the form is not submitted
    And the visitor sees the corresponding password error

  Scenario: Reject a password the API considers invalid
    Given a registration request reaches the API
    When the password is empty, shorter than 4 characters, or longer than 100 characters
    Then the API responds with 400 Bad Request
    And no user is stored
    And no activation email is sent

  Scenario Outline: Registration page is reachable without signing in
    When the visitor opens "<entry point>"
    Then the registration page is shown

    Examples:
      | entry point                         |
      | the home page registration link     |
      | the account menu Register item      |
      | the /account/register address       |
```

## UI

- Route: `/account/register`. Available to anonymous visitors.
- Fields: Username, Email, New password, New password confirmation, and a Register button.
- Show a password strength bar as the password is typed. It does not block submit.
- Do not call the API until client validation passes.
- On success, show a success toast. Do not sign the visitor in.
- On API failure (duplicate login or email, or invalid password), show the error and leave the account unchanged.

## API

`POST /api/register`

- Authentication: none.
- Request body: `login`, `email`, `password`, `langKey`.
- Success: `201 Created`, empty body.
- Duplicate activated login: `400` with a login-already-used error.
- Duplicate activated email: `400` with an email-already-used error.
- Invalid password length: `400` with an invalid-password error.
- The response must not include the password or the activation key.

## Data

Table `jhi_user`, plus `jhi_user_authority` for the role.

| Column | On successful registration |
| --- | --- |
| `login` | Required, unique, lowercase, max 50 |
| `password_hash` | Required, hash only (60 characters) |
| `email` | Unique, lowercase, 5–254 characters |
| `activated` | `false` |
| `activation_key` | Generated, not returned to the client |
| `lang_key` | `en` from the registration form |
| authority | `ROLE_USER` |

`first_name`, `last_name`, and `image_url` stay empty for this form.

## Definition of done

- [ ] Anonymous visitor can open the registration page and submit a valid account.
- [ ] Invalid input is stopped on the form with the messages in the acceptance criteria.
- [ ] API enforces duplicate login, duplicate email, and password length.
- [ ] User row matches the data table above, and the password is hashed.
- [ ] Activation email is sent only after a successful save.
- [ ] Acceptance criteria above pass, and SDET test cases under `test-cases/` are added or updated for this story.

## Notes for test

Add SDET test cases in `docs/feature-tickets/registration/test-cases/`. Cover the UI form, `POST /api/register`, and the `jhi_user` / `jhi_user_authority` rows.

Known copy mismatch to assert as current behavior, not as a new requirement: the email field’s “too long” message says 50 characters, while the field allows 254.
