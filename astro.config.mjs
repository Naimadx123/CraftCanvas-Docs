// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import catppuccin from '@catppuccin/starlight';

export default defineConfig({
  // Set SITE_URL to the public production URL when connecting a domain.
  site: process.env.SITE_URL,
  output: 'static',
  trailingSlash: 'always',
  integrations: [
    starlight({
      title: 'CraftCanvas',
      description: 'Graphical Minecraft menus in YAML for Paper and Folia, with resource-pack artwork and a public Java/Kotlin API.',
      logo: { src: './src/assets/logo.svg', alt: 'CraftCanvas' },
      social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/Naimadx123/CraftCanvas' }],
      editLink: { baseUrl: 'https://github.com/Naimadx123/CraftCanvas-Docs/edit/main/' },
      tableOfContents: { minHeadingLevel: 2, maxHeadingLevel: 3 },
      sidebar: [
        {
          label: 'Start here',
          items: [
            { label: 'Introduction', slug: 'start/introduction' },
            { label: 'Installation', slug: 'start/installation' },
            { label: 'Your first screen', slug: 'getting-started/first-screen' },
            { label: 'Commands and permissions', slug: 'start/commands' },
          ],
        },
        {
          label: 'Screen configuration',
          items: [
            { label: 'Components and layout', slug: 'guides/components' },
            { label: 'YAML reference', slug: 'reference/yaml' },
            { label: 'Variables and formatting', slug: 'configuration/variables' },
            { label: 'Actions', slug: 'configuration/actions' },
            { label: 'Conditions', slug: 'configuration/conditions' },
            { label: 'Themes and templates', slug: 'configuration/reuse' },
            { label: 'Local page variants', slug: 'guides/local-variants' },
            { label: 'Screen fonts', slug: 'guides/fonts' },
            { label: 'Animations', slug: 'guides/animations' },
            { label: 'Vanilla sprites', slug: 'guides/sprites' },
            { label: 'Inventory workbenches', slug: 'guides/crafting' },
            { label: 'Automatic item models', slug: 'guides/item-icons' },
            { label: 'Interactive UI', slug: 'guides/interactive-ui' },
            { label: 'Experimental shader hover', slug: 'guides/shader-hover' },
          ],
        },
        {
          label: 'Resources and maintenance',
          items: [
            { label: 'Resource packs', slug: 'resources/resource-pack' },
            { label: 'Integrations', slug: 'resources/integrations' },
            { label: 'Player storage', slug: 'configuration/storage' },
            { label: 'Renderer limits', slug: 'reference/book-renderer' },
            { label: 'Troubleshooting', slug: 'resources/troubleshooting' },
            { label: 'Upgrading', slug: 'resources/migration' },
          ],
        },
        {
          label: 'Developer API',
          items: [
            { label: 'Quick start', slug: 'developers/quick-start' },
            { label: 'API reference', slug: 'developers/api' },
            { label: 'Sessions and presentation', slug: 'developers/sessions' },
            { label: 'GUI controls', slug: 'developers/gui' },
            { label: 'Dynamic lists', slug: 'developers/dynamic-lists' },
            { label: 'Screen catalog', slug: 'developers/screen-catalog' },
            { label: 'Callbacks and providers', slug: 'developers/callbacks' },
            { label: 'Typed callback parameters', slug: 'developers/typed-callbacks' },
            { label: 'Async actions', slug: 'developers/async-actions' },
            { label: 'Addon bundles', slug: 'developers/bundles' },
            { label: 'Reloading', slug: 'developers/reload' },
            { label: 'Events', slug: 'developers/events' },
            { label: 'Building from source', slug: 'developers/building' },
          ],
        },
      ],
      plugins: [catppuccin({
        dark: { flavor: 'mocha', accent: 'sky' },
        light: { flavor: 'latte', accent: 'sky' },
      })],
    }),
  ],
});
