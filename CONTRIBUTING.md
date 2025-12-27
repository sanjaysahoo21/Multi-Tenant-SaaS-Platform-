# Contributing to WorkStack

Thank you for considering contributing to our project! Here are some guidelines to help you get started.

## Code of Conduct

Please be respectful and constructive in all interactions.

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in Issues
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, Docker version, etc.)

### Suggesting Features

1. Check if the feature has been suggested
2. Open an issue describing:
   - The problem it solves
   - Proposed solution
   - Alternative approaches considered

### Pull Requests

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes following our coding standards
4. Write/update tests as needed
5. Update documentation
6. Commit with clear, descriptive messages
7. Push to your fork and submit a pull request

## Development Setup

1. Clone the repository
2. Install dependencies:
   - Java 17+
   - Node.js 18+
   - Docker & Docker Compose
   - PostgreSQL (via Docker)

3. Start development environment:
   ```bash
   docker compose up -d postgres
   cd backend && mvn spring-boot:run
   cd frontend && npm install && npm run dev
   ```

## Coding Standards

### Backend (Java)
- Follow Java naming conventions
- Use 4 spaces for indentation
- Add JavaDoc for public methods
- Write unit tests for new features
- Keep methods focused and under 50 lines

### Frontend (React)
- Use functional components with hooks
- Follow React best practices
- Use 2 spaces for indentation
- Add PropTypes or TypeScript types
- Keep components small and reusable

### Git Commits
- Use conventional commits format:
  - `feat:` for new features
  - `fix:` for bug fixes
  - `docs:` for documentation
  - `refactor:` for code refactoring
  - `test:` for test additions/changes
  - `chore:` for maintenance tasks

Example: `feat(backend): Add user profile endpoint`

## Testing

- Run backend tests: `cd backend && mvn test`
- Run frontend tests: `cd frontend && npm test`
- Ensure all tests pass before submitting PR

## Documentation

- Update README.md for new features
- Add API documentation for new endpoints
- Include code comments for complex logic
- Update CHANGELOG.md

## Questions?

Feel free to open an issue for any questions or clarifications!
