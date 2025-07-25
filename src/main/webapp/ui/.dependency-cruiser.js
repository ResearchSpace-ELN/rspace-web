/** @type {import('dependency-cruiser').IConfiguration} */
module.exports = {
  forbidden: [
    {
      name: "compatible-licenses",
      comment:
        "Third-party code should adhere to a license that is compatible with our own",
      severity: "error",
      from: {},
      to: {
        licenseNot: [
          "MIT",
          "BSD",
          "Apache",
          "Hippocratic",
          "ISC",
          "MPL",
          "GPL",
        ],
        pathNot: [
          // posthog-js doesn't properly encode its license in package.json
          "node_modules/posthog-js",
        ],
      },
    },

    {
      name: "axios-only",
      comment: "Only src/common/axios.js should import axios",
      severity: "error",
      from: {
        // only src/common/axios.js and test files...
        pathNot: "^(src/common/axios\\.ts|.*\\.test\\.js)$",
      },
      to: {
        // ...should import axios
        path: "node_modules/axios/dist/node/axios.cjs",
      },
    },

    {
      name: "react-image-editor is deprecated",
      severity: "error",
      from: {
        pathNot: [
          // once the old gallery is removed, we can remove the dependency and stop using `--force` when calling `npm install`
          "^src/Gallery",
        ],
      },
      to: {
        path: "@toast-ui/react-image-editor",
      },
    },

    // this rule is disabled because cycles are generally not an issue, but the rule can be useful when debugging some issues.
    /*
    {
      name: 'no-circular',
      severity: 'warn',
      comment:
        'This dependency is part of a circular relationship. You might want to revise ' +
        'your solution (i.e. use dependency inversion, make sure the modules have a single responsibility) ',
      from: {},
      to: {
        circular: true
      }
    },
*/

    {
      name: "no-orphans",
      comment:
        "This is an orphan module - it's likely not used (anymore?). Either use it or " +
        "remove it. If it's logical this module is an orphan (i.e. it's a config file), " +
        "add an exception for it in your dependency-cruiser configuration. By default " +
        "this rule does not scrutinize dot-files (e.g. .eslintrc.js), TypeScript declaration " +
        "files (.d.ts), tsconfig.json and some of the babel and webpack configs.",
      severity: "warn",
      from: {
        orphan: true,
        pathNot: [
          "(^|/).[^/]+.(js|cjs|mjs|ts|json)$", // dot files
          ".d.ts$", // TypeScript declaration files
          "(^|/)tsconfig.json$", // TypeScript config
          "(^|/)(babel|webpack).config.(js|cjs|mjs|ts|json)$", // other configs
        ],
      },
      to: {},
    },

    {
      name: "no-unreachable-from-root",
      severity: "warn",
      from: {
        path: ["src"],
      },
      to: {
        path: "src",
        pathNot: [
          "__tests__|__mocks__|test-stubs|node_modules",
          // this list comes from webpack.config.js
          "src/App.js",
          "./src/components/PublicPages/IdentifierPublicPage.js",
          "src/eln/apps/index.js",
          "src/eln/gallery/index.js",
          "src/eln/AppBar.js",
          "src/my-rspace/directory/groups/Autoshare/MemberAutoshareStatusWrapper.js",
          "src/CreateGroup/CreateGroup.js",
          "src/my-rspace/directory/groups/MyLabGroups.js",
          "src/system-ror/RoRIntegration.js",
          "src/Export/ExportModal.js",
          "src/my-rspace/directory/groups/GroupEditBar.js",
          "src/Toolbar/Workspace/Toolbar.js",
          "src/Toolbar/Notebook/Toolbar.js",
          "src/Toolbar/StructuredDocument/Toolbar.js",
          "src/Toolbar/FileTreeToolbar.js",
          "src/Toolbar/Gallery/Toolbar.js",
          "src/system-groups/NewLabGroup.js",
          "src/tinyMCE/sidebarInfo.js",
          "src/tinyMCE/previewInfo.js",
          "src/components/UserDetails.js",
          "src/my-rspace/directory/groups/GroupUserActivity.js",
          "src/my-rspace/profile/GroupActivity.js",
          "src/my-rspace/profile/AccountActivity.js",
          "src/my-rspace/profile/OAuthTrigger.js",
          "src/my-rspace/profile/ConnectedAppsTrigger.js",
          "src/my-rspace/profile/GroupsTable.js",
          "src/tinyMCE/SnapGene/snapGeneDialog.js",
          "src/components/ToastMessage.js",
          "src/tinyMCE/internallink.js",
          "src/tinyMCE/shortcutsPlugin/shortcuts.js",
          "src/tinyMCE/pyrat/Pyrat.js",
          "src/tinyMCE/clustermarket/index.js",
          "src/tinyMCE/omero/index.js",
          "src/tinyMCE/jove/index.js",
          "src/components/BaseSearch.js",
          "src/components/ConfirmationDialog.js",
          "src/Gallery/imageEditorDialog.js",
          "src/eln-inventory-integration/MaterialsListing/MaterialsListing.js",
          "src/eln-inventory-integration/AssociatedInventoryRecords/index.js",
          "src/eln/sysadmin/users/index.js",
          // stores/defintion only export types so depcruise thinks it's never used, which at runtime it isn't
          "stores/definitions",
          // src/assets contains icon variants that may be useful in the future
          "^src/assets",
        ],
        reachable: false,
      },
    },

    {
      name: "no-deprecated-core",
      comment:
        "A module depends on a node core module that has been deprecated. Find an alternative - these are " +
        "bound to exist - node doesn't deprecate lightly.",
      severity: "warn",
      from: {},
      to: {
        dependencyTypes: ["core"],
        path: [
          "^(v8/tools/codemap)$",
          "^(v8/tools/consarray)$",
          "^(v8/tools/csvparser)$",
          "^(v8/tools/logreader)$",
          "^(v8/tools/profile_view)$",
          "^(v8/tools/profile)$",
          "^(v8/tools/SourceMap)$",
          "^(v8/tools/splaytree)$",
          "^(v8/tools/tickprocessor-driver)$",
          "^(v8/tools/tickprocessor)$",
          "^(node-inspect/lib/_inspect)$",
          "^(node-inspect/lib/internal/inspect_client)$",
          "^(node-inspect/lib/internal/inspect_repl)$",
          "^(async_hooks)$",
          "^(punycode)$",
          "^(domain)$",
          "^(constants)$",
          "^(sys)$",
          "^(_linklist)$",
          "^(_stream_wrap)$",
        ],
      },
    },

    {
      name: "not-to-deprecated",
      comment:
        "This module uses a (version of an) npm module that has been deprecated. Either upgrade to a later " +
        "version of that module, or find an alternative. Deprecated modules are a security risk.",
      severity: "warn",
      from: {},
      to: {
        dependencyTypes: ["deprecated"],
      },
    },

    {
      name: "no-duplicate-dep-types",
      comment:
        "Likely this module depends on an external ('npm') package that occurs more than once " +
        "in your package.json i.e. bot as a devDependencies and in dependencies. This will cause " +
        "maintenance problems later on.",
      severity: "warn",
      from: {},
      to: {
        moreThanOneDependencyType: true,
        // as it's pretty common to have a type import be a type only import
        // _and_ (e.g.) a devDependency - don't consider type-only dependency
        // types for this rule
        dependencyTypesNot: ["type-only"],
      },
    },

    {
      name: "not-to-spec",
      comment:
        "This module depends on a spec (test) file. The sole responsibility of a spec file is to test code. " +
        "If there's something in a spec that's of use to other modules, it doesn't have that single " +
        "responsibility anymore. Factor it out into (e.g.) a separate utility/ helper or a mock.",
      severity: "error",
      from: {},
      to: {
        path: ".(spec|test).(js|mjs|cjs|ts|ls|coffee|litcoffee|coffee.md)$",
      },
    },

    {
      name: "not-to-dev-dep",
      severity: "error",
      comment:
        "This module depends on an npm package from the 'devDependencies' section of your " +
        "package.json. It looks like something that ships to production, though. To prevent problems " +
        "with npm packages that aren't there on production declare it (only!) in the 'dependencies'" +
        "section of your package.json. If this module is development only - add it to the " +
        "from.pathNot re of the not-to-dev-dep rule in the dependency-cruiser configuration",
      from: {
        path: "^(src)",
        pathNot:
          "__tests__|.(spec|test).(js|mjs|cjs|ts|tsx|ls|coffee|litcoffee|coffee.md)$",
      },
      to: {
        dependencyTypes: ["npm-dev"],
        pathNot: "@babel",
      },
    },

    {
      name: "optional-deps-used",
      severity: "info",
      comment:
        "This module depends on an npm package that is declared as an optional dependency " +
        "in your package.json. As this makes sense in limited situations only, it's flagged here. " +
        "If you're using an optional dependency here by design - add an exception to your" +
        "dependency-cruiser configuration.",
      from: {},
      to: {
        dependencyTypes: ["npm-optional"],
      },
    },

    {
      name: "peer-deps-used",
      comment:
        "This module depends on an npm package that is declared as a peer dependency " +
        "in your package.json. This makes sense if your package is e.g. a plugin, but in " +
        "other cases - maybe not so much. If the use of a peer dependency is intentional " +
        "add an exception to your dependency-cruiser configuration.",
      severity: "warn",
      from: {},
      to: {
        dependencyTypes: ["npm-peer"],
      },
    },

    {
      name: "Inventory-only",
      comment:
        "Modules containing generic code or ELN code should not depend on Inventory components or Inventory-specific code.",
      from: {
        // only this whitelist of paths...
        pathNot:
          "src/Inventory|src/Router.js|src/eln-inventory-integration|src/stores|src/App.js|src/components/PublicPages/IdentifierPublicPage.js",
      },
      to: {
        // ...are the modules that should be accessing these Inventory-specific modules
        path: "src/Inventory|src/stores/stores|src/stores/use-stores",
      },
    },

    {
      name: "Apps-only",
      comment:
        "Modules containing generic code, Inventory code, or other parts of the ELN should not depend on code specific to the apps page.",
      severity: "error",
      from: {
        pathNot: "src/eln/apps",
      },
      to: {
        path: "src/eln/apps",
      },
    },

    {
      name: "Sysadmin-only",
      comment:
        "Modules containing generic code, Inventory code, or other parts of the ELN should not depend on code specific to the sysadmin page.",
      from: {
        pathNot: "src/eln/sysadmin",
      },
      to: {
        path: "src/eln/sysadmin",
      },
    },

    {
      name: "definitions-are-leaves",
      comment:
        "src/stores/definitions should only depend on themselves or utils",
      from: {
        path: "src/stores/definitions",
      },
      to: {
        pathNot: "src/stores/definitions|src/util|fast-check|babel",
      },
    },

    {
      name: "utils-are-leaves",
      comment:
        "src/util should not depend on other parts of the codebase; they should be pure functions and simple data structures.",
      from: {
        path: "src/util",
      },
      to: {
        pathNot: "src/util|babel|jest-dom|fast-check|mobx|react",
      },
    },

    {
      name: "restrict-assets-dependencies",
      comment:
        "src/assets should only depend on a small subset of the codebase.",
      severity: "error",
      from: {
        path: "src/assets",
      },
      to: {
        pathNot:
          "src/assets|src/util|react|node_modules/@mui/material/node/SvgIcon|node_modules/@mui/material/node/utils/index.js",
      },
    },

    {
      name: "Public pages must not use stores",
      comment:
        "Users may not be authenticated on the public pages and so we must not depend on the global stores as they assume the user is.",
      from: {
        path: "src/components/PublicPages",
      },
      to: {
        path: "src/stores/stores",
        reachable: true,
      },
    },
  ],
  allowed: [],
  required: [],
  options: {
    /* conditions specifying which files not to follow further when encountered:
       - path: a regular expression to match
       - dependencyTypes: see https://github.com/sverweij/dependency-cruiser/blob/main/doc/rules-reference.md#dependencytypes-and-dependencytypesnot
       for a complete list
    */
    doNotFollow: {
      path: "node_modules",
    },

    /* conditions specifying which dependencies to exclude
       - path: a regular expression to match
       - dynamic: a boolean indicating whether to ignore dynamic (true) or static (false) dependencies.
          leave out if you want to exclude neither (recommended!)
    */
    // exclude : {
    //   path: '',
    //   dynamic: true
    // },

    /* pattern specifying which files to include (regular expression)
       dependency-cruiser will skip everything not matching this pattern
    */
    // includeOnly : '',

    /* dependency-cruiser will include modules matching against the focus
       regular expression in its output, as well as their neighbours (direct
       dependencies and dependents)
    */
    // focus : '',

    /* list of module systems to cruise */
    // moduleSystems: ['amd', 'cjs', 'es6', 'tsd'],

    /* prefix for links in html and svg output (e.g. 'https://github.com/you/yourrepo/blob/develop/'
       to open it on your online repo or `vscode://file/${process.cwd()}/` to
       open it in visual studio code),
     */
    // prefix: '',

    /* false (the default): ignore dependencies that only exist before typescript-to-javascript compilation
       true: also detect dependencies that only exist before typescript-to-javascript compilation
       "specify": for each dependency identify whether it only exists before compilation or also after
     */
    // tsPreCompilationDeps: false,

    /*
       list of extensions to scan that aren't javascript or compile-to-javascript.
       Empty by default. Only put extensions in here that you want to take into
       account that are _not_ parsable.
    */
    // extraExtensionsToScan: [".json", ".jpg", ".png", ".svg", ".webp"],

    /* if true combines the package.jsons found from the module up to the base
       folder the cruise is initiated from. Useful for how (some) mono-repos
       manage dependencies & dependency definitions.
     */
    // combinedDependencies: false,

    /* if true leave symlinks untouched, otherwise use the realpath */
    // preserveSymlinks: false,

    /* TypeScript project file ('tsconfig.json') to use for
       (1) compilation and
       (2) resolution (e.g. with the paths property)

       The (optional) fileName attribute specifies which file to take (relative to
       dependency-cruiser's current working directory). When not provided
       defaults to './tsconfig.json'.
     */
    tsConfig: {
      fileName: "tsconfig.json",
    },

    /* Webpack configuration to use to get resolve options from.

       The (optional) fileName attribute specifies which file to take (relative
       to dependency-cruiser's current working directory. When not provided defaults
       to './webpack.conf.js'.

       The (optional) `env` and `arguments` attributes contain the parameters to be passed if
       your webpack config is a function and takes them (see webpack documentation
       for details)
     */
    webpackConfig: {
      fileName: "webpack.config.js",
      env: {},
      arguments: {},
    },

    /* Babel config ('.babelrc', '.babelrc.json', '.babelrc.json5', ...) to use
      for compilation (and whatever other naughty things babel plugins do to
      source code). This feature is well tested and usable, but might change
      behavior a bit over time (e.g. more precise results for used module
      systems) without dependency-cruiser getting a major version bump.
     */
    //babelConfig: {
    //fileName: 'babel.config.js'
    //},

    /* List of strings you have in use in addition to cjs/ es6 requires
       & imports to declare module dependencies. Use this e.g. if you've
       re-declared require, use a require-wrapper or use window.require as
       a hack.
    */
    // exoticRequireStrings: [],
    /* options to pass on to enhanced-resolve, the package dependency-cruiser
       uses to resolve module references to disk. You can set most of these
       options in a webpack.conf.js - this section is here for those
       projects that don't have a separate webpack config file.

       Note: settings in webpack.conf.js override the ones specified here.
     */
    enhancedResolveOptions: {
      /* List of strings to consider as 'exports' fields in package.json. Use
         ['exports'] when you use packages that use such a field and your environment
         supports it (e.g. node ^12.19 || >=14.7 or recent versions of webpack).

         If you have an `exportsFields` attribute in your webpack config, that one
         will have precedence over the one specified here.
      */
      exportsFields: ["exports"],
      /* List of conditions to check for in the exports field. e.g. use ['imports']
         if you're only interested in exposed es6 modules, ['require'] for commonjs,
         or all conditions at once `(['import', 'require', 'node', 'default']`)
         if anything goes for you. Only works when the 'exportsFields' array is
         non-empty.

        If you have a 'conditionNames' attribute in your webpack config, that one will
        have precedence over the one specified here.
      */
      conditionNames: ["import", "require", "node", "default"],
      /*
         The extensions, by default are the same as the ones dependency-cruiser
         can access (run `npx depcruise --info` to see which ones that are in
         _your_ environment. If that list is larger than what you need (e.g.
         it contains .js, .jsx, .ts, .tsx, .cts, .mts - but you don't use
         TypeScript you can pass just the extensions you actually use (e.g.
         [".js", ".jsx"]). This can speed up the most expensive step in
         dependency cruising (module resolution) quite a bit.
       */
      // extensions: [".js", ".jsx", ".ts", ".tsx", ".d.ts"],
      /*
         If your TypeScript project makes use of types specified in 'types'
         fields in package.jsons of external dependencies, specify "types"
         in addition to "main" in here, so enhanced-resolve (the resolver
         dependency-cruiser uses) knows to also look there. You can also do
         this if you're not sure, but still use TypeScript. In a future version
         of dependency-cruiser this will likely become the default.
       */
      mainFields: ["main", "types", "typings"],
    },
    reporterOptions: {
      dot: {
        /* pattern of modules that can be consolidated in the detailed
           graphical dependency graph. The default pattern in this configuration
           collapses everything in node_modules to one folder deep so you see
           the external modules, but not the innards your app depends upon.
         */
        collapsePattern: "node_modules/(@[^/]+/[^/]+|[^/]+)",

        /* Options to tweak the appearance of your graph.See
           https://github.com/sverweij/dependency-cruiser/blob/main/doc/options-reference.md#reporteroptions
           for details and some examples. If you don't specify a theme
           don't worry - dependency-cruiser will fall back to the default one.
        */
        // theme: {
        //   graph: {
        //     /* use splines: "ortho" for straight lines. Be aware though
        //       graphviz might take a long time calculating ortho(gonal)
        //       routings.
        //    */
        //     splines: "true"
        //   },
        //   modules: [
        //     {
        //       criteria: { matchesFocus: true },
        //       attributes: {
        //         fillcolor: "lime",
        //         penwidth: 2,
        //       },
        //     },
        //     {
        //       criteria: { matchesFocus: false },
        //       attributes: {
        //         fillcolor: "lightgrey",
        //       },
        //     },
        //     {
        //       criteria: { matchesReaches: true },
        //       attributes: {
        //         fillcolor: "lime",
        //         penwidth: 2,
        //       },
        //     },
        //     {
        //       criteria: { matchesReaches: false },
        //       attributes: {
        //         fillcolor: "lightgrey",
        //       },
        //     },
        //     {
        //       criteria: { source: "^src/model" },
        //       attributes: { fillcolor: "#ccccff" }
        //     },
        //     {
        //       criteria: { source: "^src/view" },
        //       attributes: { fillcolor: "#ccffcc" }
        //     },
        //   ],
        //   dependencies: [
        //     {
        //       criteria: { "rules[0].severity": "error" },
        //       attributes: { fontcolor: "red", color: "red" }
        //     },
        //     {
        //       criteria: { "rules[0].severity": "warn" },
        //       attributes: { fontcolor: "orange", color: "orange" }
        //     },
        //     {
        //       criteria: { "rules[0].severity": "info" },
        //       attributes: { fontcolor: "blue", color: "blue" }
        //     },
        //     {
        //       criteria: { resolved: "^src/model" },
        //       attributes: { color: "#0000ff77" }
        //     },
        //     {
        //       criteria: { resolved: "^src/view" },
        //       attributes: { color: "#00770077" }
        //     }
        //   ]
        // }
      },
      archi: {
        /* pattern of modules that can be consolidated in the high level
          graphical dependency graph. If you use the high level graphical
          dependency graph reporter (`archi`) you probably want to tweak
          this collapsePattern to your situation.
        */
        collapsePattern:
          "^(packages|src|lib|app|bin|test(s?)|spec(s?))/[^/]+|node_modules/(@[^/]+/[^/]+|[^/]+)",

        /* Options to tweak the appearance of your graph.See
           https://github.com/sverweij/dependency-cruiser/blob/main/doc/options-reference.md#reporteroptions
           for details and some examples. If you don't specify a theme
           for 'archi' dependency-cruiser will use the one specified in the
           dot section (see above), if any, and otherwise use the default one.
         */
        // theme: {
        // },
      },
      text: {
        highlightFocused: true,
      },
    },
  },
};
// generated: dependency-cruiser@14.1.2 on 2023-10-20T14:17:19.415Z
