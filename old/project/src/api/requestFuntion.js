import service from "./request.js";

/**
 * 根据菜名查询每日价格
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function selectDayByName(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据菜名查询各省昨日价格
 * @param {Object}
 * @param {String} 
 * @returns {Promise}
 */
export function selectProvinceByName(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据时间查询全国蔬菜价格
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function selectByQuarter(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据出口量查询交易市场Top5
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function exportTop5(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据省份统计各类产品销量
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function calendar(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据省份识别价格异常的市场
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function radar(query, url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据时间查询全国蔬菜价格散点图数据
 * @param {Object}
 * @param {String} 
 * @returns {Promise} 
 */
export function selectScatterVa(query, url) {
  console.log("请求参数:", query);
  
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 根据菜名和年份获取各省菜品价格信息
 * @param {String} dishName 菜品名称
 * @param {Number} year 年份
 * @returns {Promise} 
 */
export function getDishPricesByProvince(dishName, year) {
  return service({
    method: "GET",
    url: "/api/dishprice/provincePrices",
    params: {
      dishName,
      year
    },
    timeout: 15000
  });
}

/**
 * 获取农产品分类列表
 * @returns {Promise} 
 */
export function getProductCategories() {
  return service({
    method: "GET",
    url: "/api/categories",
  });
}

/**
 * 根据分类ID获取产品列表
 * @param {Number} categoryId 分类ID
 * @returns {Promise} 
 */
export function getProductsByCategory(categoryId) {
  return service({
    method: "GET",
    url: "/api/products/category",
    params: { categoryId }
  });
}

/**
 * 获取产品价格趋势数据
 * @param {Number} productId 产品ID
 * @param {String} timeRange 时间范围(week/month/year)
 * @returns {Promise} 
 */
export function getProductPriceTrend(productId, timeRange) {
  return service({
    method: "GET",
    url: "/api/products/priceTrend",
    params: {
      productId,
      timeRange
    }
  });
}

/**
 * 获取所有蔬菜类型
 * @returns {Promise} 
 */
export function getAllVegetableTypes() {
  return service({
    method: "GET",
    url: "/vegetable/type2/all"
  });
}

/**
 * 按年份查询季度数据
 * @param {Object} data 包含年份的查询参数
 * @returns {Promise} 
 */
export function getQuarterDataByYear(data) {
  return service({
    method: "POST",
    url: "/quarter/byYear",
    data: data
  });
}

/**
 * 获取特定蔬菜的省级价格数据，带重试机制
 * @param {String} dishName 蔬菜名称
 * @param {Number} year 年份
 * @returns {Promise} 
 */
export function getVegetableProvincePrice(dishName, year) {
  return new Promise(async (resolve, reject) => {
    let retries = 3; // 最大重试次数
    
    const attempt = async () => {
      try {
        const response = await service({
          method: "GET",
          url: "/api/dishprice/provincePrices",
          params: {
            dishName,
            year
          },
          timeout: 15000 // 覆盖默认超时时间
        });
        resolve(response);
      } catch (error) {
        if (retries > 0 && error.code === 'ECONNABORTED') {
          retries--;
          console.log(`请求超时，正在进行第${3-retries}次重试...`);
          // 等待1秒后重试
          setTimeout(() => {
            attempt();
          }, 1000);
        } else {
          reject(error);
        }
      }
    };
    
    attempt();
  });
}

//当日农产品种类数量
export function selectKind(query,url) {
  return service({
    method: "GET",
    url: url,
    data: query,
  });
}

//根据农产品查询当日交易量
export function selectVolume(query,url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

//根据农产品查询批发市场数量
export function selectMarket(query,url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

/**
 * 获取7天天气预报
 * @param {String} location - 城市代码
 * @returns {Promise} 
 */
export function get7DayWeather(location) {
  return service({
    method: "GET",
    url: "/api/ai/7d",
    params: { location }
  });
}


//根据农产品查询当日交易额
export function selectValue(query,url) {
  return service({
    method: "POST",
    url: url,
    data: query,
  });
}

//当日农产品种类数量
export function getProvince(query,url) {
  return service({
    method: "GET",
    url: url,
    data: query,
  });
}

//获取user顶部卡片数据
export function getTitle(query,url) {
  return service({
    method: "GET",
    url: url,
    data: query,
  });
}


/**
 * 获取蔬菜价格预测
 * @param {Object} predictionData - 预测数据
 * @returns {Promise} 
 */
export function predictVegetablePrice(predictionData) {
  return service({
    method: "POST",
    url: "/api/ai/predictPrice",
    data: predictionData
  });
}

/**
 * 获取全国预测价格
 * @param {String} vegetable - 蔬菜名称
 * @returns {Promise} 
 */
export function getNationalPredictedPrices(vegetable) {
  return service({
    method: "GET",
    url: "/api/ai/predictPrice/national",
    params: { vegetable }
  });
}