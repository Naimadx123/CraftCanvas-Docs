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
      social: [{ icon: 'github', label: 'Documentation on GitHub', href: 'https://github.com/Naimadx123/CraftCanvas-Docs' }],
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
            { label: 'Configuration overview', slug: 'configuration/overview' },
            { label: 'Main configuration', slug: 'configuration/config' },
            { label: 'Components and layout', slug: 'guides/components' },
            { label: 'Variables and formatting', slug: 'configuration/variables' },
            { label: 'Actions', slug: 'configuration/actions' },
            { label: 'Conditions', slug: 'configuration/conditions' },
            { label: 'Themes and templates', slug: 'configuration/reuse' },
          ],
        },
        {
          label: 'More screen features',
          collapsed: true,
          items: [
            { label: 'Local page variants', slug: 'guides/local-variants' },
            { label: 'Screen fonts', slug: 'guides/fonts' },
            { label: 'Animations', slug: 'guides/animations' },
            { label: 'Vanilla sprites', slug: 'guides/sprites' },
            { label: 'Inventory workbenches', slug: 'guides/crafting' },
            { label: 'Automatic item models', slug: 'guides/item-icons' },
            { label: 'Interactive UI', slug: 'guides/interactive-ui' },
            { label: 'Button hover', slug: 'guides/shader-hover' },
          ],
        },
        {
          label: 'Resources and maintenance',
          items: [
            { label: 'Resource packs', slug: 'resources/resource-pack' },
            { label: 'Integrations', slug: 'resources/integrations' },
            { label: 'Player storage', slug: 'configuration/storage' },
            { label: 'Troubleshooting', slug: 'resources/troubleshooting' },
            { label: 'Upgrading', slug: 'resources/migration' },
          ],
        },
        {
          label: 'Reference',
          collapsed: true,
          items: [
            { label: 'YAML fields', slug: 'reference/yaml' },
            { label: 'Renderer limits', slug: 'reference/book-renderer' },
          ],
        },
        {
          label: 'Developer API',
          collapsed: true,
          items: [
            { label: 'Your first addon', slug: 'developers/quick-start' },
            { label: 'API overview', slug: 'developers/api' },
            { label: 'Threads and async work', slug: 'developers/threading' },
            { label: 'Code-defined screens', slug: 'developers/code-screens' },
            { label: 'Typed definition builders', slug: 'developers/definition-builders' },
            { label: 'Component state and edits', slug: 'developers/component-state' },
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
            { label: 'Method reference', slug: 'developers/api-reference' },
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
