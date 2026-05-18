# Config Submodule Setup

The application imports environment-specific configuration from the private
`AAC-ai/backend-config` repository mounted at:

```text
src/main/resources/config
```

If this directory is empty, Spring Boot cannot load files such as `jwt.yml`,
`oidc.yml`, `openai.yml`, `db.yml`, and `prompts.yml`. In that case tests or
local startup can fail with `ConfigDataResourceNotFoundException`.

## Local Setup

Initialize the config submodule after cloning this repository:

```bash
git submodule update --init --recursive
```

To refresh it later:

```bash
git submodule update --remote --recursive
```

If GitHub prompts for authentication, use a GitHub account or token that can
read the private `AAC-ai/backend-config` repository.

## CI Setup

GitHub Actions checks out the private submodule with:

```yaml
submodules: true
token: ${{ secrets.SUBMODULE_TOKEN }}
```

The `AAC-ai/backend` repository must define this Actions secret:

```text
SUBMODULE_TOKEN=<GitHub PAT with read access to AAC-ai/backend-config>
```

Use a fine-grained personal access token with the smallest scope possible:

```text
Resource owner: AAC-ai
Repository access: Only select repositories
Repository: backend-config
Repository permissions:
  Contents: Read-only
```

Do not commit real config files, tokens, database passwords, or other secrets
directly to this repository.
