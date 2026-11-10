# FEAT-002 — Account activation

| Field | Value |
| --- | --- |
| Issue type | Story |
| Status | Ready for development |
| Priority | High |
| Reporter | Business analyst |
| Assignee | Unassigned |
| Components | UI, API, Database |
| Labels | `activation` `account` `public` |
| Fix version | Unscheduled |
| Depends on | [FEAT-001](../registration/FEAT-001-user-registration.md) — user registration |
| Related | Sign-in (confirms the account is on; not a separate build in this story) |

## Summary

A person who registered opens the activation link from the email. The account is turned on, the activation key is cleared, and they are asked to sign in. A missing, unknown, or already-used key does not change any account and the page explains that activation failed.

## User story

As a person who just registered, I want to open the activation link in my email, so that my account is turned on and I can sign in.

## Background

FEAT-001 creates an inactive user, stores an activation key, and sends an email. The link in that email points at `/account/activate?key=<activation key>`.

This story starts when that link is opened. It does not cover composing the email or creating the account. It does not cover the sign-in form itself, except that a successful activation must leave the account able to authenticate, and a failed activation must leave it unable to.

## Scope

- Activation page at `/account/activate` that reads `key` from the query string and calls the API.
- Public `GET /api/activate?key=` that turns the matching account on and clears its activation key.
- Update of `jhi_user`: `activated` becomes true and `activation_key` becomes empty.

## Out of scope

- Registration and sending the activation email (FEAT-001).
- The sign-in screen, password reset, and admin user management.
- Resending an activation email.
- Activating an account from the admin user-management screen.

## Business rules

1. The activation page is public. No signed-in session is required.
2. The page calls the API as soon as it loads, using the `key` query parameter.
3. A key matches at most one user. On a match, that user is activated and the key is removed in the same update.
4. After a successful activation the same link must not activate anyone again, because the key is gone.
5. An unknown key, a missing key, or a key that was already used does not activate or modify any user.
6. Login, email, password hash, and `ROLE_USER` are left unchanged.
7. The API does not return the activation key or the password.
8. The visitor is not signed in by activation. They follow the link to the sign-in page themselves.

## Acceptance criteria

```gherkin
Feature: Account activation

  Background:
    Given an inactive account exists for login "jane.doe" and email "jane.doe@example.com"
    And that account has activation key "activation-key-1"
    And that account has authority "ROLE_USER"
    And the visitor is not signed in

  Scenario: Open a valid activation link
    When the visitor opens "/account/activate?key=activation-key-1"
    Then the activation page is shown
    And a success message says the account has been activated
    And a sign-in link to "/login" is shown
    And the account "jane.doe" is activated
    And the activation key for "jane.doe" is cleared
    And the login, email, password hash, and ROLE_USER authority are unchanged
    And the visitor is not signed in

  Scenario: Activated account can sign in
    Given the visitor has successfully opened "/account/activate?key=activation-key-1"
    When the visitor signs in as "jane.doe" with the password chosen at registration
    Then sign-in succeeds

  Scenario: Use the same activation link a second time
    Given the visitor already opened "/account/activate?key=activation-key-1" successfully
    When the visitor opens "/account/activate?key=activation-key-1" again
    Then a failure message says the user could not be activated
    And the account "jane.doe" stays activated
    And the activation key stays empty
    And no other account is changed

  Scenario: Open a link with an unknown key
    When the visitor opens "/account/activate?key=does-not-exist"
    Then a failure message says the user could not be activated
    And the account "jane.doe" stays inactive
    And the activation key for "jane.doe" is still "activation-key-1"

  Scenario: Open the activation page with no key
    When the visitor opens "/account/activate"
    Then a failure message says the user could not be activated
    And the account "jane.doe" stays inactive
    And the activation key for "jane.doe" is still "activation-key-1"

  Scenario: API activates the matching user
    When a client calls "GET /api/activate?key=activation-key-1" with no authentication
    Then the response is successful
    And the response body does not contain the password or the activation key
    And the account "jane.doe" is activated
    And the activation key for "jane.doe" is cleared

  Scenario: API rejects an unknown key
    When a client calls "GET /api/activate?key=does-not-exist"
    Then the response is an error
    And no user is activated
    And no activation key is cleared

  Scenario: Inactive account cannot sign in
    Given the activation link has not been opened
    When the visitor tries to sign in as "jane.doe" with the registration password
    Then sign-in is rejected
    And the account stays inactive
```

## UI

- Route: `/account/activate`. Available to anonymous visitors.
- On load, read `key` from the query string and call `GET /api/activate?key=`.
- Heading: “Activation”.
- Success: a success alert, “Your user account has been activated.”, and a link to `/login` labeled as sign in.
- Failure: a danger alert, “Your user could not be activated. Please use the registration form to sign up.”
- Show only one of those alerts. Do not show a form on this page.

## API

`GET /api/activate?key={activationKey}`

- Authentication: none.
- Success: the matching user is activated, their activation key is cleared, and the response body is empty.
- Unknown, missing, or already-used key: error response, and no user row is changed.
- The response must not include the password or the activation key.

The link in the FEAT-001 activation email is `{baseUrl}/account/activate?key={activationKey}`.

## Data

Table `jhi_user`.

| Column | After a successful activation |
| --- | --- |
| `activated` | `true` |
| `activation_key` | cleared |
| `login`, `email`, `password_hash` | unchanged |
| `ROLE_USER` on `jhi_user_authority` | unchanged |

A failed activation leaves every column unchanged.

## Definition of done

- [ ] Opening a valid activation link turns that account on, clears the key, and shows the success message with a sign-in link.
- [ ] A second use of the same link, an unknown key, and a missing key show the failure message and do not modify accounts.
- [ ] An inactive account cannot sign in. After this story’s success path, the same credentials can.
- [ ] Acceptance criteria above pass, and SDET test cases under `test-cases/` are added or updated for this story.

## Notes for test

Add SDET test cases in `docs/feature-tickets/activation/test-cases/`. Cover the activation page, `GET /api/activate`, and the `jhi_user` update.

The account under test must come from registration (FEAT-001) or an equivalent inactive row with a known activation key and password. Assert the key is null after success, not a new key.
