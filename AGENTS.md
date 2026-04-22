<!-- GSD:project-start source:PROJECT.md -->
## Project

**Pawn Service Backend**

Pawn Service Backend is a Spring Boot service that powers an e-commerce style pawn marketplace workflow for product browsing, cart management, ordering, and payment processing. It serves admin and customer roles, integrates with Stripe for checkout, and exposes APIs consumed by the frontend application. The system also includes operational integrations for Redis, S3-compatible storage, and OpenAI-backed chat flows.

**Core Value:** A customer can reliably browse products, place an order, and complete payment end-to-end without data inconsistency.

### Constraints

- **Tech stack**: Continue with Java 21, Spring Boot, MySQL, Redis, and current adapters — preserve compatibility with existing services
- **Integration compatibility**: Keep Stripe webhook contract and existing frontend API compatibility — avoid breaking deployed clients
- **Security**: Remove insecure local defaults from production paths — reduce credential and token misuse risk
- **Delivery scope**: Focus on backend reliability and operability in this milestone — avoid broad architecture rewrites
<!-- GSD:project-end -->

<!-- GSD:stack-start source:STACK.md -->
## Technology Stack

Technology stack not yet documented. Will populate after codebase mapping or first phase.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

| Skill | Description | Path |
|-------|-------------|------|
| java-coding-standards | "Java coding standards for Spring Boot services: naming, immutability, Optional usage, streams, exceptions, generics, and project layout." | `.agents/skills/java-coding-standards/SKILL.md` |
| spring-boot-engineer | Generates Spring Boot 3.x configurations, creates REST controllers, implements Spring Security 6 authentication flows, sets up Spring Data JPA repositories, and configures reactive WebFlux endpoints. Use when building Spring Boot 3.x applications, microservices, or reactive Java applications; invoke for Spring Data JPA, Spring Security 6, WebFlux, Spring Cloud integration, Java REST API design, or Microservices Java architecture. | `.agents/skills/spring-boot-engineer/SKILL.md` |
| spring-boot-testing | Expert Spring Boot 4 testing specialist that selects the best Spring Boot testing techniques for your situation with Junit 6 and AssertJ. | `.agents/skills/spring-boot-testing/SKILL.md` |
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
