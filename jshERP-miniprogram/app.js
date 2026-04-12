const storage = require('./utils/storage')
const { DEFAULT_API_BASE_URL } = require('./utils/config')

App({
  globalData: {
    apiBaseUrl: DEFAULT_API_BASE_URL
  },

  onLaunch() {
    const savedBaseUrl = storage.getBaseUrl()
    if (savedBaseUrl) {
      this.globalData.apiBaseUrl = savedBaseUrl
    }
  }
})
