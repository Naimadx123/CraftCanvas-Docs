# CraftCanvas documentation website

Documentation for [CraftCanvas](https://github.com/Naimadx123/CraftCanvas), built with [Astro](https://astro.build), [Starlight](https://starlight.astro.build) and the Catppuccin theme, matching the Voxen documentation stack.

This project lives in the separate [Naimadx123/CraftCanvas-Docs](https://github.com/Naimadx123/CraftCanvas-Docs) repository. The website builds independently; it does not need the CraftCanvas source tree, Java or Gradle.

## Local development

Use Node.js 24 (the exact version is pinned in `.node-version`). From the repository root:

```sh
npm ci
npm run dev
```

Open `http://localhost:4321`. To generate and inspect the production site:

```sh
npm run build
npm run preview
```

Static output is written to `dist/`, including a Pagefind search index. Search is available in the production preview.

## Cloudflare Pages

Connect `Naimadx123/CraftCanvas-Docs` as a **Pages** project using these build settings:

| Setting | Value |
| --- | --- |
| Production branch | `main` |
| Framework preset | Astro |
| Root directory | Repository root (leave blank) |
| Build command | `npm run build` |
| Build output directory | `dist` |
| Environment variable | `NODE_VERSION=24.19.0` |

Pages installs dependencies before building. Deploy the generated static output; no Cloudflare adapter, Worker, bindings or secrets are required. See [Cloudflare's Astro guide](https://developers.cloudflare.com/pages/framework-guides/deploy-an-astro-site/).

Once the production address is known, optionally set `SITE_URL` to the full HTTPS address, such as your assigned Pages hostname or custom domain, to enable canonical URLs. Keep `SITE_URL` set to the production URL for preview builds too.

## Editing documentation

- Pages live in `src/content/docs/`; `.md` and `.mdx` files need a frontmatter `title`.
- The landing page is `src/content/docs/index.mdx`.
- Navigation, social links and theme settings live in `astro.config.mjs`. Add each new guide to the sidebar.
- Internal links use site paths, for example `/reference/yaml/#click-slider`.
- Links to plugin YAML, Java, Kotlin and tooling use the CraftCanvas source repository.
- `src/examples/variants.yml` is a copy of the plugin's class selection example, embedded in the local variants guide. Update it when the plugin example changes.
- “Edit page” links target this documentation repository's `main` branch.

The previous Markdown guides have been migrated into this structure. The old table of contents is retained at `/contents/`; the sidebar also includes the newer font, storage, interactive UI and developer guides.
