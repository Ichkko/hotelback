# Production secrets and environment discipline

## Required runtime variables

The `prod` profile now fails fast unless all of these environment variables are present:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_TOKEN`

## Rules

1. Do not commit production secrets into the repository.
2. Do not use fallback values for production credentials or JWT secrets.
3. Inject secrets from your deployment platform or a dedicated secret manager.
4. Keep `SPRING_PROFILES_ACTIVE=prod` in real production deployments.
5. Use schema migrations for production changes; `ddl-auto` is set to `validate` in production.

## Recommended secret sources

- Cloud secret managers such as AWS Secrets Manager, GCP Secret Manager, or Azure Key Vault.
- Container platform secrets such as Kubernetes Secrets, ECS task secrets, or Docker Swarm secrets.
- CI/CD protected environment variables for short-lived deployment injection.

## Operational notes

- Local development should use the `dev` profile and local-only fallback values.
- Production logging keeps SQL output disabled to avoid leaking sensitive query data.
- Rotate `JWT_TOKEN` with a value that is at least 32 characters long.
