Page({
  openTasks() {
    wx.navigateTo({
      url: '/pages/manufacturing-task/manufacturing-task'
    })
  },

  openMaterialIssues() {
    wx.navigateTo({
      url: '/pages/material-issue/material-issue'
    })
  }
})
