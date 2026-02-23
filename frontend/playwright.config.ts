import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright configuration for Teaching Management System E2E tests
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './tests/e2e',

  /* Run tests in files in parallel */
  fullyParallel: true,

  /* Fail the build on CI if you accidentally left test.only in the source code */
  forbidOnly: !!process.env.CI,

  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,

  /* Opt out of parallel tests on CI */
  workers: process.env.CI ? 1 : undefined,

  /* Reporter configuration */
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'playwright-results.xml' }],
    ['json', { outputFile: 'playwright-results.json' }],
    ['list']
  ],

  /* Shared settings for all the projects */
  use: {
    /* Base URL for navigation */
    baseURL: process.env.BASE_URL || 'http://localhost:5173',

    /* Collect trace when retrying the failed test */
    trace: 'on-first-retry',

    /* Take screenshot on failure */
    screenshot: 'only-on-failure',

    /* Record video on failure */
    video: 'retain-on-failure',

    /* Action timeout */
    actionTimeout: 10000,

    /* Navigation timeout */
    navigationTimeout: 30000,

    /* Locale and timezone for Chinese system */
    locale: 'zh-CN',
    timezoneId: 'Asia/Shanghai',
  },

  /* Configure projects for major browsers */
  projects: [
    /* Setup project - runs before other projects */
    {
      name: 'setup',
      testMatch: /.*\.setup\.ts/,
    },

    /* Test with no authentication - auth tests and workflow tests */
    {
      name: 'chromium-no-auth',
      use: {
        ...devices['Desktop Chrome'],
      },
      testMatch: [/.*\/auth\/.*\.spec\.ts/, /.*\/workflows\/.*\.spec\.ts/],
    },

    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        /* Use prepared auth state */
        storageState: 'playwright/.auth/teacher.json',
      },
      dependencies: ['setup'],
      testIgnore: [/.*\/auth\/.*\.spec\.ts/, /.*\/workflows\/.*\.spec\.ts/],
    },

    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        storageState: 'playwright/.auth/teacher.json',
      },
      dependencies: ['setup'],
      testIgnore: [/.*\/auth\/.*\.spec\.ts/, /.*\/workflows\/.*\.spec\.ts/],
    },

    {
      name: 'webkit',
      use: {
        ...devices['Desktop Safari'],
        storageState: 'playwright/.auth/teacher.json',
      },
      dependencies: ['setup'],
      testIgnore: [/.*\/auth\/.*\.spec\.ts/, /.*\/workflows\/.*\.spec\.ts/],
    },

    /* Mobile viewport tests */
    {
      name: 'mobile-chrome',
      use: {
        ...devices['Pixel 5'],
        storageState: 'playwright/.auth/student.json',
      },
      dependencies: ['setup'],
      testIgnore: [/.*\/auth\/.*\.spec\.ts/, /.*\/workflows\/.*\.spec\.ts/],
    },
  ],

  /* Run local dev server before starting the tests */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },

  /* Output folder for test artifacts */
  outputDir: 'test-results/',

  /* Global timeout for tests */
  timeout: 60000,

  /* Expect timeout */
  expect: {
    timeout: 10000,
  },
})
