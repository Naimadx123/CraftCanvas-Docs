# CraftCanvas documentation

These guides cover installing CraftCanvas, building Minecraft menus in YAML and writing addons in Java or Kotlin. Start with [installation](src/content/docs/start/installation.md), [your first screen](src/content/docs/getting-started/first-screen.md) or [your first addon](src/content/docs/developers/quick-start.mdx).

## Run the site locally

Use the Node.js version in `.node-version`. In this directory, run:

```sh
npm ci
npm run dev
```

Open `http://localhost:4321` to see your changes. To try the site search, build the site and start the preview:

```sh
npm run build
npm run preview
```

The build writes the site and its search index to `dist/`.

## Edit a guide

The guides live in `src/content/docs/`. Give each Markdown or MDX page a `title` in its frontmatter. When you add a guide, add its path to the sidebar in `astro.config.mjs`.

Put downloadable examples in `public/examples/` and link to them at `/examples/`. The local variants guide also embeds `src/examples/variants.yml`. If you change that screen, update its downloadable copy in `public/examples/screens/variants.yml` too.

## Deploy to Cloudflare Pages

Connect the documentation repository to Cloudflare Pages. Choose the Astro preset and use these settings:

| Setting | Value |
| --- | --- |
| Production branch | `main` |
| Root directory | Leave blank |
| Build command | `npm run build` |
| Build output directory | `dist` |
| Environment variable | `NODE_VERSION=24.19.0` |

Set `SITE_URL` to your public HTTPS address if you want canonical links. Use that same address for preview builds. See [Cloudflare's Astro guide](https://developers.cloudflare.com/pages/framework-guides/deploy-an-astro-site/) for deployment details.
