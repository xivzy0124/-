import { defineStore } from 'pinia'

// ==========================================
// 1. 地理位置 Store (管理地图联动)
// ==========================================
export const mapLocation = defineStore('mapLocationPinia', {
  state: () => {
    return {
      currentProvince: '河南省', // 省
      currentCity: '郑州市',     // 市
      currentDistrict: '',     // 区县 (默认为空)
    }
  },
  actions: {
    // 设置省份 -> 自动重置市、区
    setCurrentProvince(province) {
      this.currentProvince = province
      this.currentCity = ''
      this.currentDistrict = ''
    },

    // 设置城市 -> 自动重置区
    setCurrentCity(city) {
      this.currentCity = city
      this.currentDistrict = ''
    },

    // 设置区县
    setCurrentDistrict(district) {
      this.currentDistrict = district
    },

    // 重置回默认状态
    resetLocation() {
      this.currentProvince = '河南省'
      this.currentCity = '郑州市'
      this.currentDistrict = ''
    },
  },
})

// ==========================================
// 2. 农作物/蔬菜 Store (管理业务筛选)
// ==========================================
export const mapProduct = defineStore('mapProductPinia', {
  state: () => {
    return {
      currentProduct: '大白菜', // 当前选中的蔬菜
    }
  },
  actions: {
    // 设置农作物
    setCurrentProduct(product) {
      this.currentProduct = product
    },

    // 重置农作物 (可选)
    resetProduct() {
      this.currentProduct = '大白菜'
    }
  },
})