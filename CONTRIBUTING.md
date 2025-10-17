# Contributing to Re:Auth

Hey there! 👋

We’re super excited that you want to contribute to **Re:Auth**. It’s an open-source, high-performance Minecraft
implementation, and we couldn’t do it without awesome community members like you. By contributing, you’re agreeing to
follow our **[Code of Conduct](https://www.google.com/search?q=CODE_OF_CONDUCT.md)**—basically, just be kind,
respectful, and collaborative.

## How You Can Help

There are lots of ways to pitch in:

* **Report bugs** – Help us squash those pesky issues.
* **Submit code** – Add new features, fix bugs, or improve existing functionality.
* **Improve docs** – Make our documentation clearer and easier for everyone.
* **Share feedback** – Test new stuff and tell us what works (or doesn’t).

Even small contributions make a big difference!

---

## Getting Started

### Set Up Your Development Environment

Here’s how to get your local setup ready:

1. **Install the prerequisites**
   Make sure you have **JDK 21** or later installed.

2. **Clone the repo**

   ```bash
   git clone https://github.com/Reprivatize/ReAuth-Backend.git
   cd ReAuth
   ```

3. **Open the project**
   Open the `Re:Auth` folder in your favorite IDE (IntelliJ IDEA or VS Code with Kotlin plugins work great). Gradle
   should automatically detect the modules for you.

4. **Run the project**

   ```bash
   ./gradlew run
   ```

   This will build everything you need and start the Re:Auth client.

---

## Reporting a Bug

The more details you give, the faster we can fix it. When opening an issue
on [GitHub Issues](https://github.com/Reprivatize/ReAuth-Backend/issues), try to include:

* A **short, clear title** describing the problem
* **Steps to reproduce** the issue
* **What you expected** to happen vs. **what actually happened**
* Your **Re:Auth version**
* Your **OS and system info**
* Any **logs or screenshots** that help explain the problem

---

## Submitting Code Changes

We love contributions! Before you start, make sure you’re familiar with our coding standards.

### Pull Request Process

1. **Fork the repo** on GitHub.

2. **Create a new branch** with a descriptive name, like `feature/cool-new-thing` or `fix/bug-123`:

   ```bash
   git checkout -b fix/issue-123-bug-name
   ```

3. **Make your changes** in the relevant module(s).

4. **Write clear commit messages**.

5. **Push your branch**:

   ```bash
   git push origin your-branch-name
   ```

6. **Open a Pull Request** from your branch to the `main` branch.

7. **Explain your changes** in the PR description, and if it fixes a bug, reference it (e.g., `Closes #123`).

### Code Style

We follow standard Kotlin conventions. Make your code clean, readable, and consistent. You can use your IDE’s formatter
or a linter like **Qodana** (configured in `qodana.yml`).

---

## Need Help?

Got questions, stuck on setup, or want to brainstorm an idea? Then just create a *
*[Discussion](https://github.com/mtctx/Re:Auth/discussions)**.

Thanks for helping make Re:Auth even better! We really appreciate it. 💜
