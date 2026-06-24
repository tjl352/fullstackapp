#!/usr/bin/env python3
"""Generate a PDF documenting all user-facing features of fullstackapp."""

from fpdf import FPDF
from pathlib import Path

OUTPUT = Path(__file__).resolve().parent.parent / "docs" / "fullstackapp-user-features.pdf"


class FeaturesPDF(FPDF):
    def header(self):
        if self.page_no() > 1:
            self.set_font("Helvetica", "I", 9)
            self.set_text_color(120, 120, 120)
            self.cell(0, 8, "fullstackapp - User Features Guide", align="R", new_x="LMARGIN", new_y="NEXT")
            self.ln(2)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "I", 9)
        self.set_text_color(120, 120, 120)
        self.cell(0, 10, f"Page {self.page_no()}", align="C")

    def cover_title(self, title, subtitle):
        self.add_page()
        self.ln(50)
        self.set_font("Helvetica", "B", 28)
        self.set_text_color(33, 37, 41)
        self.multi_cell(0, 14, title, align="C")
        self.ln(8)
        self.set_font("Helvetica", "", 14)
        self.set_text_color(73, 80, 87)
        self.multi_cell(0, 8, subtitle, align="C")
        self.ln(20)
        self.set_font("Helvetica", "", 11)
        self.set_text_color(108, 117, 125)
        self.multi_cell(0, 7, "Generated from the application source code\nJHipster 8.11.0 | React + Spring Boot | JWT Authentication", align="C")

    def section(self, title):
        self.ln(4)
        self.set_font("Helvetica", "B", 16)
        self.set_text_color(13, 110, 253)
        self.multi_cell(0, 10, title)
        self.ln(2)

    def subsection(self, title):
        self.ln(2)
        self.set_font("Helvetica", "B", 12)
        self.set_text_color(33, 37, 41)
        self.multi_cell(0, 8, title)
        self.ln(1)

    def body(self, text):
        self.set_font("Helvetica", "", 11)
        self.set_text_color(33, 37, 41)
        self.multi_cell(0, 6, text)
        self.ln(1)

    def bullet(self, text):
        self.set_font("Helvetica", "", 11)
        self.set_text_color(33, 37, 41)
        x = self.get_x()
        self.cell(6, 6, "-")
        self.multi_cell(0, 6, text)
        self.set_x(x)

    def table_header(self, cols, widths):
        self.set_font("Helvetica", "B", 10)
        self.set_fill_color(233, 236, 239)
        self.set_text_color(33, 37, 41)
        for col, w in zip(cols, widths):
            self.cell(w, 8, col, border=1, fill=True)
        self.ln()

    def table_row(self, cols, widths):
        self.set_font("Helvetica", "", 10)
        self.set_text_color(33, 37, 41)
        for col, w in zip(cols, widths):
            self.cell(w, 8, col, border=1)
        self.ln()


def build_pdf():
    pdf = FeaturesPDF()
    pdf.set_auto_page_break(auto=True, margin=20)
    pdf.set_margins(20, 20, 20)

    pdf.cover_title("fullstackapp", "User Features Guide")

    pdf.add_page()
    pdf.section("1. Application Overview")
    pdf.body(
        "fullstackapp is a full-stack web application built with JHipster 8.11.0. "
        "It combines a React single-page application (SPA) frontend with a Spring Boot "
        "backend API. Users authenticate with JWT (JSON Web Token) tokens, and access "
        "to pages and API endpoints is controlled by role-based authorization."
    )
    pdf.body("Key characteristics:")
    pdf.bullet("Application type: Monolith (frontend and backend in one deployable unit)")
    pdf.bullet("Frontend: React with React Router, Reactstrap, and Redux")
    pdf.bullet("Backend: Spring Boot with REST APIs")
    pdf.bullet("Database: H2 (development) / PostgreSQL (production)")
    pdf.bullet("Authentication: JWT with optional 'Remember me' session persistence")
    pdf.bullet("Custom business entities: None configured yet (scaffold ready for future entities)")

    pdf.section("2. User Roles & Access")
    pdf.body("The application defines two authority roles that control what each user can see and do:")
    pdf.table_header(["Role", "Description", "Access Level"], [35, 55, 90])
    pdf.table_row(["Anonymous", "Visitor not signed in", "Home, login, register, password reset"], [35, 55, 90])
    pdf.table_row(["ROLE_USER", "Standard authenticated user", "Account settings, password change"], [35, 55, 90])
    pdf.table_row(["ROLE_ADMIN", "Administrator", "All user features + administration panel"], [35, 55, 90])
    pdf.ln(2)
    pdf.body(
        "Protected routes redirect unauthenticated users to the sign-in page. "
        "If a signed-in user lacks the required role, they see an authorization error message."
    )

    pdf.section("3. Navigation & Layout")
    pdf.body("The top navigation bar is always visible and adapts based on login status and role:")
    pdf.bullet("Home - Returns to the welcome/homepage")
    pdf.bullet("Account menu - Sign in, register, settings, password, or sign out")
    pdf.bullet("Administration menu - Visible only to administrators")
    pdf.bullet("Entities menu - Visible to authenticated users (empty until custom entities are added)")
    pdf.bullet("Development ribbon - Shown in non-production environments to indicate dev mode")
    pdf.bullet("Toast notifications - Success and error messages appear in the top-left corner")

    pdf.section("4. Public Features (No Login Required)")

    pdf.subsection("4.1 Home Page")
    pdf.body("Route: /")
    pdf.body(
        "The homepage welcomes visitors and shows contextual information. "
        "Signed-in users see a confirmation of their login name. "
        "Visitors see links to sign in or register, plus information about default demo accounts."
    )

    pdf.subsection("4.2 Sign In")
    pdf.body("Route: /login")
    pdf.body("Opens a sign-in modal where users enter:")
    pdf.bullet("Username")
    pdf.bullet("Password")
    pdf.bullet("Remember me (optional checkbox - keeps the session active longer)")
    pdf.body("On successful login, users are redirected to the page they originally requested, or to the homepage.")
    pdf.body("Failed login attempts display an error message asking the user to check their credentials.")
    pdf.body("The modal also provides links to password reset and registration.")

    pdf.subsection("4.3 Register a New Account")
    pdf.body("Route: /account/register")
    pdf.body("New users can create an account by providing:")
    pdf.bullet("Username (1-50 characters; letters, numbers, and common symbols)")
    pdf.bullet("Email address (validated format, 5-254 characters)")
    pdf.bullet("Password (minimum 4 characters, with a visual strength indicator)")
    pdf.bullet("Password confirmation (must match the password)")
    pdf.body(
        "After registration, the account is created in a deactivated state. "
        "An activation email is sent with a link to activate the account before first sign-in."
    )

    pdf.subsection("4.4 Account Activation")
    pdf.body("Route: /account/activate?key=<activation-key>")
    pdf.body(
        "Users click the activation link from their registration email. "
        "On success, a confirmation message is shown with a link to sign in. "
        "If activation fails (invalid or expired key), an error message is displayed."
    )

    pdf.subsection("4.5 Password Reset")
    pdf.body("Request route: /account/reset/request")
    pdf.body("Finish route: /account/reset/finish?key=<reset-key>")
    pdf.body("The password reset flow has two steps:")
    pdf.bullet("Step 1 - User enters their registered email address and submits a reset request")
    pdf.bullet("Step 2 - User clicks the link in the reset email and sets a new password with confirmation")
    pdf.body("A password strength bar helps users choose a stronger password during reset.")

    pdf.add_page()
    pdf.section("5. Authenticated User Features (ROLE_USER)")

    pdf.subsection("5.1 Account Settings")
    pdf.body("Route: /account/settings")
    pdf.body("Signed-in users can update their profile information:")
    pdf.bullet("First name (required, up to 50 characters)")
    pdf.bullet("Last name (required, up to 50 characters)")
    pdf.bullet("Email address (required, validated format)")
    pdf.body("Changes are saved immediately and a success notification is shown.")

    pdf.subsection("5.2 Change Password")
    pdf.body("Route: /account/password")
    pdf.body("Signed-in users can change their password by providing:")
    pdf.bullet("Current password (for verification)")
    pdf.bullet("New password (minimum 4 characters, with strength indicator)")
    pdf.bullet("New password confirmation")
    pdf.body("An incorrect current password shows an error. A successful change shows a confirmation toast.")

    pdf.subsection("5.3 Sign Out")
    pdf.body("Route: /logout")
    pdf.body(
        "Signing out clears the JWT token and ends the session. "
        "The user is returned to the public homepage."
    )

    pdf.section("6. Administrator Features (ROLE_ADMIN)")
    pdf.body(
        "Administrators have access to an Administration dropdown in the navigation bar "
        "with tools for managing users and monitoring the application."
    )

    pdf.subsection("6.1 User Management")
    pdf.body("Route: /admin/user-management")
    pdf.body("Administrators can fully manage application users:")
    pdf.bullet("View a paginated, sortable list of all users (ID, login, email, status, roles, dates)")
    pdf.bullet("Create new users with login, name, email, password, role assignments, and activation status")
    pdf.bullet("Edit existing user details and roles")
    pdf.bullet("View individual user details")
    pdf.bullet("Delete users (cannot delete your own account)")
    pdf.bullet("Activate or deactivate users with a single click")
    pdf.bullet("Refresh the user list")
    pdf.body("Available roles for assignment: ROLE_USER and ROLE_ADMIN.")

    pdf.subsection("6.2 Application Metrics")
    pdf.body("Route: /admin/metrics")
    pdf.body("Displays real-time performance and runtime metrics including:")
    pdf.bullet("JVM memory usage")
    pdf.bullet("JVM thread information and thread dump")
    pdf.bullet("Garbage collector statistics")
    pdf.bullet("HTTP request metrics")
    pdf.bullet("Endpoint request counts")
    pdf.bullet("Datasource connection pool metrics")
    pdf.bullet("Cache metrics (if caching is enabled)")
    pdf.bullet("System (OS/process) metrics")
    pdf.body("Metrics can be refreshed on demand.")

    pdf.subsection("6.3 Health Checks")
    pdf.body("Route: /admin/health")
    pdf.body(
        "Shows the health status of application components (e.g., database, disk space, mail). "
        "Each component is marked UP (healthy) or DOWN (unhealthy). "
        "Clicking a component opens a detail modal with additional diagnostic information."
    )

    pdf.subsection("6.4 Configuration")
    pdf.body("Route: /admin/configuration")
    pdf.body(
        "Lists all Spring Boot configuration beans and their properties. "
        "Useful for verifying application settings and troubleshooting."
    )

    pdf.subsection("6.5 Log Management")
    pdf.body("Route: /admin/logs")
    pdf.body(
        "Displays all configured application loggers and their current log levels. "
        "Administrators can change log levels at runtime (e.g., switch a package to DEBUG) "
        "without restarting the application."
    )

    pdf.subsection("6.6 API Documentation (Swagger UI)")
    pdf.body("Route: /admin/docs")
    pdf.body(
        "Embeds the Swagger UI interface for exploring and testing all REST API endpoints. "
        "Available when OpenAPI documentation is enabled in the application profile."
    )

    pdf.subsection("6.7 Database Console (Development Only)")
    pdf.body("Link: /h2-console/")
    pdf.body(
        "In development mode, administrators can access the embedded H2 database console "
        "to inspect and query the database directly. This is not available in production."
    )

    pdf.add_page()
    pdf.section("7. Default Demo Accounts")
    pdf.body("The application ships with two pre-configured accounts for testing:")
    pdf.table_header(["Login", "Password", "Role"], [50, 50, 80])
    pdf.table_row(["admin", "admin", "ROLE_ADMIN (full access)"], [50, 50, 80])
    pdf.table_row(["user", "user", "ROLE_USER (standard access)"], [50, 50, 80])
    pdf.ln(2)
    pdf.body("These accounts are intended for development and demonstration purposes only.")

    pdf.section("8. Email Notifications")
    pdf.body("The application sends automated emails for key account events:")
    pdf.bullet("Account creation - Sent when a new user registers")
    pdf.bullet("Account activation - Contains the activation link for new accounts")
    pdf.bullet("Password reset - Contains the reset link with a time-limited key")
    pdf.body(
        "Email delivery requires a configured mail server. In development, "
        "emails may be logged to the console rather than actually sent."
    )

    pdf.section("9. Security Features")
    pdf.bullet("JWT-based stateless authentication")
    pdf.bullet("BCrypt password hashing")
    pdf.bullet("Role-based access control on routes and API endpoints")
    pdf.bullet("Account activation required before login (for self-registered users)")
    pdf.bullet("Time-limited password reset keys")
    pdf.bullet("Automatic redirect to login for protected pages")
    pdf.bullet("Authorization error page for insufficient permissions")

    pdf.section("10. Future Extensibility")
    pdf.body(
        "The application is scaffolded with JHipster entity support. When business entities "
        "are added (via the JHipster generator), authenticated users will automatically gain "
        "CRUD screens accessible from the Entities menu in the navigation bar. "
        "Currently, no custom entities have been defined."
    )

    pdf.section("11. Quick Reference - Routes")
    widths = [65, 115]
    pdf.table_header(["Route", "Feature"], widths)
    routes = [
        ("/", "Homepage"),
        ("/login", "Sign in"),
        ("/logout", "Sign out"),
        ("/account/register", "Register"),
        ("/account/activate", "Activate account"),
        ("/account/reset/request", "Request password reset"),
        ("/account/reset/finish", "Complete password reset"),
        ("/account/settings", "User settings"),
        ("/account/password", "Change password"),
        ("/admin/user-management", "Manage users (admin)"),
        ("/admin/metrics", "Application metrics (admin)"),
        ("/admin/health", "Health checks (admin)"),
        ("/admin/configuration", "Spring configuration (admin)"),
        ("/admin/logs", "Log levels (admin)"),
        ("/admin/docs", "API documentation (admin)"),
    ]
    for route, feature in routes:
        pdf.table_row([route, feature], widths)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    pdf.output(str(OUTPUT))
    return OUTPUT


if __name__ == "__main__":
    path = build_pdf()
    print(f"PDF created: {path}")
