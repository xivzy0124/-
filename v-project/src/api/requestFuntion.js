import service from './request.js'
/**
 * 获取7天天气预报
 * @param {String} location - 城市代码
 * @returns {Promise}
 */
export function get7DayWeather(location) {
  return service({
    method: 'GET',
    url: '/api/weather/7day',
    params: { location },
  })
}

/**
 * 根据省份和蔬菜名称获取年度价格趋势
 * @param {String} provinceName - 省份名称
 * @param {String} varietyName - 蔬菜名称
 * @returns {Promise}
 */
export function getYearlyTrendByProvince(provinceName, varietyName) {
  return service({
    method: 'GET',
    url: '/api/price/yearlyTrend',
    params: {
      provinceName,
      varietyName,
    },
  })
}


/**
 * 获取所有蔬菜类型
 * @returns {Promise}
 */
export function getAllVaegettableTypes() {
  return service({
    method: 'GET',
    url: '/api/vaegettableType/selectAll',
  })
}


/**
 * 文本转语音播放函数
 * @param {string} text - 你想要播放的文字内容
 */
export async function speak(text) {
  // --- 配置区域 (内部存放) ---
  // 1. 在这里填入你的 SiliconFlow API Key
  const API_KEY = "sk-zxeavsjuzkbxhzgyjduoefhuseoeerfzxxiicoztfyoudpcy";

  // 2. 内部配置 (已根据文档填好默认值)
  const API_URL = "https://api.siliconflow.cn/v1/audio/speech";
  const MODEL = "FunAudioLLM/CosyVoice2-0.5B";
  const VOICE = "FunAudioLLM/CosyVoice2-0.5B:anna"; // 默认沉稳男声 alex
  // -------------------------

  if (!text) {
    console.warn("请输入文本");
    return;
  }

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        model: MODEL,
        input: text,       // 这里的 input 对应文档要求的参数
        voice: VOICE,
        response_format: "mp3",
        speed: 1.0,
        gain: 0.0
      })
    });

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status} ${await response.text()}`);
    }

    // 获取音频流并播放
    const blob = await response.blob();
    const audioUrl = URL.createObjectURL(blob);
    const audio = new Audio(audioUrl);

    // 播放音频
    await audio.play();

    // 播放完释放内存
    audio.onended = () => URL.revokeObjectURL(audioUrl);

  } catch (error) {
    console.error("播放出错:", error);
  }
}


















/**
 * 根据菜名查询每日价格
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function selectDayByName(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据菜名查询各省昨日价格
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function selectProvinceByName(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据时间查询全国蔬菜价格
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function selectByQuarter(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据出口量查询交易市场Top5
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function exportTop5(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据省份统计各类产品销量
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function calendar(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据省份识别价格异常的市场
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function radar(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据时间查询全国蔬菜价格散点图数据
 * @param {Object}
 * @param {String}
 * @returns {Promise}
 */
export function selectScatterVa(query, url) {
  console.log('请求参数:', query)

  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

/**
 * 根据菜名和年份获取各省菜品价格信息
 * @param {String} dishName 菜品名称
 * @param {Number} year 年份
 * @returns {Promise}
 */
export function getDishPricesByProvince(dishName, year) {
  return service({
    method: 'GET',
    url: '/api/dishprice/provincePrices',
    params: {
      dishName,
      year,
    },
    timeout: 15000,
  })
}

/**
 * 获取农产品分类列表
 * @returns {Promise}
 */
export function getProductCategories() {
  return service({
    method: 'GET',
    url: '/api/categories',
  })
}

/**
 * 根据分类ID获取产品列表
 * @param {Number} categoryId 分类ID
 * @returns {Promise}
 */
export function getProductsByCategory(categoryId) {
  return service({
    method: 'GET',
    url: '/api/products/category',
    params: { categoryId },
  })
}

/**
 * 获取产品价格趋势数据
 * @param {Number} productId 产品ID
 * @param {String} timeRange 时间范围(week/month/year)
 * @returns {Promise}
 */
export function getProductPriceTrend(productId, timeRange) {
  return service({
    method: 'GET',
    url: '/api/products/priceTrend',
    params: {
      productId,
      timeRange,
    },
  })
}

/**
 * 获取所有蔬菜类型
 * @returns {Promise}
 */
export function getAllVegetableTypes() {
  return service({
    method: 'GET',
    url: '/vegetable/type2/all',
  })
}

/**
 * 按年份查询季度数据
 * @param {Object} data 包含年份的查询参数
 * @returns {Promise}
 */
export function getQuarterDataByYear(data) {
  return service({
    method: 'POST',
    url: '/quarter/byYear',
    data: data,
  })
}









/**
 * 获取特定蔬菜的省级价格数据，带重试机制
 * @param {String} dishName 蔬菜名称
 * @param {Number} year 年份
 * @returns {Promise}
 */
export function getVegetableProvincePrice(dishName, year) {
  return new Promise(async (resolve, reject) => {
    let retries = 3 // 最大重试次数

    const attempt = async () => {
      try {
        const response = await service({
          method: 'GET',
          url: '/api/dishprice/provincePrices',
          params: {
            dishName,
            year,
          },
          timeout: 15000, // 覆盖默认超时时间
        })
        resolve(response)
      } catch (error) {
        if (retries > 0 && error.code === 'ECONNABORTED') {
          retries--
          console.log(`请求超时，正在进行第${3 - retries}次重试...`)
          // 等待1秒后重试
          setTimeout(() => {
            attempt()
          }, 1000)
        } else {
          reject(error)
        }
      }
    }

    attempt()
  })
}

//当日农产品种类数量
export function selectKind(query, url) {
  return service({
    method: 'GET',
    url: url,
    data: query,
  })
}

//根据农产品查询当日交易量
export function selectVolume(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

//根据农产品查询批发市场数量
export function selectMarket(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}


//根据农产品查询当日交易额
export function selectValue(query, url) {
  return service({
    method: 'POST',
    url: url,
    data: query,
  })
}

//当日农产品种类数量
export function getProvince(query, url) {
  return service({
    method: 'GET',
    url: url,
    data: query,
  })
}

//获取user顶部卡片数据
export function getTitle(query, url) {
  return service({
    method: 'GET',
    url: url,
    data: query,
  })
}

/**
 * 获取蔬菜价格预测
 * @param {Object} predictionData - 预测数据
 * @returns {Promise}
 */
export function predictVegetablePrice(predictionData) {
  return service({
    method: 'POST',
    url: '/api/ai/predictPrice',
    data: predictionData,
  })
}

/**
 * 获取全国预测价格
 * @param {String} vegetable - 蔬菜名称
 * @returns {Promise}
 */
export function getNationalPredictedPrices(vegetable) {
  return service({
    method: 'GET',
    url: '/api/ai/predictPrice/national',
    params: { vegetable },
  })
}

/**
 * 获取中国地图数据
 * @returns {Promise}
 */
export function getChinaMap() {
  return service({
    method: 'GET',
    url: '/api/map/china',
  })
}

/**
 * 获取省份地图数据
 * @param {String} adcode - 省份adcode
 * @returns {Promise}
 */
export function getProvinceMap(adcode) {
  return service({
    method: 'GET',
    url: `/api/map/province/${adcode}`,
  })
}

/**
 * 获取城市地图数据
 * @param {String} adcode - 城市adcode
 * @returns {Promise}
 */
export function getCityMap(adcode) {
  return service({
    method: 'GET',
    url: `/api/map/city/${adcode}`,
  })
}

/**
 * 获取区域名称列表
 * @param {String} adcode - 区域adcode
 * @returns {Promise}
 */
export function getRegionNames(adcode) {
  return service({
    method: 'GET',
    url: `/api/map/regions/${adcode}`,
  })
}



