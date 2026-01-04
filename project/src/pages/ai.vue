<template>
	<div class="ai-container">
		<!-- 直接使用 Head 组件 -->
		<Head />
			
		<div class="container">
			<div class="location-selector">
				<el-select 
					v-model="province"
					size="small"
					style="width: 120px"
					popper-class="blue-select"
					@change="handleProvinceChange"
				>
					<el-option
						v-for="item in provinceOptions"
						:key="item"
						:label="item"
						:value="item"
					/>
				</el-select>
				
				<el-select 
					v-model="city"
					size="small"
					style="width: 120px"
					placeholder="选择城市"
				>
					<el-option
						v-for="cityItem in cityOptions"
						:key="cityItem"
						:label="cityItem"
						:value="cityItem"
					/>
				</el-select>
				<el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
			</div>

			<section class="weekly-weather">
				<h2 class="section-title">{{city}} - 未来一周天气预报</h2>
				<div v-if="loading" class="loading">加载中...</div>
				<div v-else-if="weatherForecast.length === 0" class="no-data">请选择城市并查询天气</div>
				<div v-else class="weather-cards">
					<div v-for="day in weatherForecast" :key="day.fxDate" class="weather-card">
						<h3>{{ formatDay(day.fxDate) }}</h3>
						<div class="date">{{ formatDate(day.fxDate) }}</div>
						<div class="weather-icon">{{ getWeatherIcon(day.textDay) }}</div>
						<div class="temp">{{ day.tempMin }}°C/{{ day.tempMax }}°C</div>
						<div class="condition">{{ day.textDay }}</div>
						<div class="wind">{{ day.windDirDay }} {{ day.windScaleDay }}级</div>
					</div>
				</div>
			</section>

			<section class="bottom-section">
				<div class="map-container">
					<div class="section-title">
						<h2 id="map-title">{{ mapTitle }}</h2>
						<div class="map-controls">
							<el-button class="map-btn" @click="zoomIn">放大</el-button>
							<el-button class="map-btn" @click="zoomOut">缩小</el-button>
							<el-button class="map-btn" @click="resetView">重置视图</el-button>
							<el-button
								class="map-btn back-to-national"
								v-show="isProvinceView"
								@click="backToNationalMap"
							>
								返回全国
							</el-button>
						</div>
					</div>
					<div class="map-content">
						<div id="china-map" ref="mapContainerRef"></div>
					</div>
				</div>

				<div class="ranking-container">
					<div class="section-title">
						<h2 id="ranking-title">{{ rankingTitle }}</h2>
						<div class="vegetable-selector">
							<el-select 
								v-model="selectedVegetable"
								placeholder="请选择农产品" 
								filterable 
								class="vegetable-select"
								size="small"
							>
								<el-option-group 
									v-for="group in productOptions" 
									:key="group.oneLevel" 
									:label="group.oneLevel"
								>
									<el-option
										v-for="item in group.items"
										:key="item.varietyname"
										:label="item.varietyname"
										:value="item.varietyname"
									>
										<span>{{ item.twoLevel }} - {{ item.varietyname }}</span>
									</el-option>
								</el-option-group>
							</el-select>
						</div>
					</div>

					<div class="ranking-header">
						<div class="header-rank">排名</div>
						<div class="header-province" id="ranking-region">{{ rankingRegionHeader }}</div>
						<div class="header-price">各市平均价格</div>
					</div>

					<ul class="ranking-list" id="vegetable-ranking">
						<li v-for="(item, index) in top10Prices" :key="item.name" class="ranking-item" @click="handleRankItemClick(item)">
							<span :class="['rank', getRankClass(index)]">{{ index + 1 }}</span>
							<span class="province">{{ item.name }}</span>
							<span class="value">{{ item.value.toFixed(1) }}元/斤</span>
						</li>
					</ul>

					<div class="ranking-footer" id="ranking-footer">
						{{ rankingFooterText }}
					</div>
				</div>
			</section>
		</div>
	</div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import * as echarts from 'echarts';
import { ElMessage, ElButton } from 'element-plus';
import { getProvince, selectKind } from '/src/api/requestFuntion.js';
import { definedPerson } from '/src/store/store.js';
import Head from '/src/components/head.vue'; // 导入 Head 组件
import './ai.css'; // 导入外部CSS文件

// --- STATE ---
const mapContainerRef = ref(null);
let mapChart = null;
let currentMapZoom = 1.2;

const currentMapName = ref('china');
const currentRegionName = ref('全国');
const selectedVegetable = ref('大白菜');
const loading = ref(false);
const productOptions = ref([]);

// CSV数据存储
const cityPriceData = ref([]); // 存储市均价数据
const provincePriceData = ref([]); // 存储省均价数据

// 从 CSV 数据中提取的省份和城市选项
const provinceOptions = ref([]);
const cityOptions = ref([]);
const province = ref('');
const city = ref('');

// 根据 CSV 文件构建的省份-城市映射数据
const provinceCityMap = {
	'上海市': ['上海市'],
	'云南省': ['文山壮族苗族自治州', '昆明市', '曲靖市', '楚雄彝族自治州', '玉溪市', '红河哈尼族彝族自治州'],
	'内蒙古自治区': ['包头市', '呼和浩特市', '赤峰市', '鄂尔多斯市'],
	'北京市': ['北京市'],
	'吉林省': ['松原市', '白山市', '辽源市', '长春市'],
	'四川省': ['凉山彝族自治州', '南充市', '广安市', '成都市', '绵阳市', '达州市'],
	'天津市': ['天津市'],
	'宁夏回族自治区': ['中卫市', '吴忠市', '银川市'],
	'安徽省': ['亳州市', '六安市', '合肥市', '安庆市', '宿州市', '淮北市', '滁州市', '蚌埠市', '阜阳市', '马鞍山市'],
	'山东省': ['临沂市', '威海市', '德州市', '枣庄市', '济南市', '济宁市', '淄博市', '滨州市', '潍坊市', '菏泽市', '青岛市'],
	'山西省': ['临汾市', '大同市', '太原市', '晋城市', '朔州市', '运城市', '长治市', '阳泉市'],
	'广东省': ['东莞市', '佛山市', '广州市', '汕头市', '江门市'],
	'广西壮族自治区': ['柳州市', '百色市'],
	'新疆维吾尔自治区': ['乌鲁木齐市', '克拉玛依市', '博尔塔拉蒙古自治州', '喀什地区', '巴音郭楞蒙古自治州', '昌吉回族自治州'],
	'江苏省': ['南京市', '南通市', '常州市', '徐州市', '扬州市', '无锡市', '苏州市'],
	'江西省': ['九江市', '南昌市', '吉安市', '景德镇市'],
	'河北省': ['保定市', '唐山市', '廊坊市', '张家口市', '沧州市', '石家庄市', '秦皇岛市', '邢台市', '邯郸市'],
	'河南省': ['信阳市', '南阳市', '周口市', '商丘市', '安阳市', '洛阳市', '濮阳市', '郑州市'],
	'浙江省': ['嘉兴市', '宁波市', '杭州市', '绍兴市', '金华市'],
	'海南省': ['海口市'],
	'湖北省': ['孝感市', '武汉市', '荆州市', '襄阳市', '鄂州市', '黄冈市', '黄石市'],
	'湖南省': ['常德市', '邵阳市', '长沙市'],
	'甘肃省': ['临夏回族自治州', '兰州市', '天水市', '定西市', '武威市', '白银市', '酒泉市', '金昌市'],
	'福建省': ['厦门市', '宁德市', '福州市'],
	'西藏自治区': ['拉萨市'],
	'贵州省': ['贵阳市', '遵义市'],
	'辽宁省': ['大连市', '朝阳市', '沈阳市', '阜新市', '鞍山市'],
	'重庆市': ['重庆市'],
	'陕西省': ['咸阳市', '延安市', '西安市'],
	'青海省': ['海东市', '西宁市'],
	'黑龙江省': ['哈尔滨市', '鹤岗市', '齐齐哈尔市']
};

// 城市代码映射
const cityCodeMap = {
    '七台河市': '101051001',
    '万宁市': '101310215',
    '三亚市': '101310201',
    '三明市': '101230801',
    '三沙市': '101310301',
    '三门峡市': '101181701',
    '上海市': '101020100',
    '上饶市': '101240301',
    '东方市': '101310202',
    '东莞市': '101281601',
    '东营市': '101121201',
    '中卫市': '101170501',
    '中山市': '101281701',
    '临夏回族自治州': '101161101',
    '临汾市': '101100701',
    '临沂市': '101120901',
    '临沧市': '101291101',
    '临高县': '101310203',
    '丹东市': '101070601',
    '丽水市': '101210801',
    '丽江市': '101291401',
    '乌兰察布市': '101080401',
    '乌海市': '101080301',
    '乌鲁木齐市': '101130101',
    '乐东黎族自治县': '101310221',
    '乐山市': '101271401',
    '九江市': '101240201',
    '云林县': '2897F',
    '云浮市': '101281401',
    '五家渠市': '101131801',
    '五指山市': '101310222',
    '亳州市': '101220901',
    '仙桃市': '101201601',
    '伊春市': '101050801',
    '伊犁哈萨克自治州': '101131001',
    '佛山市': '101280800',
    '佳木斯市': '101050401',
    '保亭黎族苗族自治县': '101310214',
    '保定市': '101090201',
    '保山市': '101290501',
    '信阳市': '101180601',
    '儋州市': '101310205',
    '克孜勒苏柯尔克孜自治州': '101131501',
    '克拉玛依市': '101130201',
    '六安市': '101221501',
    '六盘水市': '101260801',
    '兰州市': '101160101',
    '兴安盟': '101081101',
    '内江市': '101271201',
    '凉山彝族自治州': '101271601',
    '包头市': '101080201',
    '北京市': '101010100',
    '北屯市': '101132101',
    '北海市': '101301301',
    '十堰市': '101201101',
    '南京市': '101190101',
    '南充市': '101270501',
    '南宁市': '101300101',
    '南平市': '101230901',
    '南投县': '101340404',
    '南昌市': '101240101',
    '南通市': '101190501',
    '南阳市': '101180701',
    '博尔塔拉蒙古自治州': '101131601',
    '厦门市': '101230201',
    '双河市': '101132201',
    '双鸭山市': '101051301',
    '可克达拉市': '101132301',
    '台东县': '101340204',
    '台中市': '101340401',
    '台北市': '101340101',
    '台南市': '101340203',
    '台州市': '101210601',
    '合肥市': '101220101',
    '吉安市': '101240601',
    '吉林市': '101060201',
    '吐鲁番市': '101130501',
    '吕梁市': '101101100',
    '吴忠市': '101170301',
    '周口市': '101181401',
    '呼伦贝尔市': '101081001',
    '呼和浩特市': '101080101',
    '和田地区': '101131301',
    '咸宁市': '101200701',
    '咸阳市': '101110200',
    '哈密市': '101131201',
    '哈尔滨市': '101050101',
    '唐山市': '101090501',
    '商丘市': '101181001',
    '商洛市': '101110601',
    '喀什地区': '101130901',
    '嘉义县': '1851B',
    '嘉义市': '101340202',
    '嘉兴市': '101210301',
    '嘉峪关市': '101161401',
    '四平市': '101060401',
    '固原市': '101170401',
    '图木舒克市': '101131701',
    '基隆市': '2CA40',
    '塔城地区': '101131101',
    '大兴安岭地区': '101050701',
    '大同市': '101100201',
    '大庆市': '101050901',
    '大理白族自治州': '101290201',
    '大连市': '101070201',
    '天水市': '101160901',
    '天津市': '101030100',
    '天门市': '101201501',
    '太原市': '101100101',
    '威海市': '101121301',
    '娄底市': '101250801',
    '孝感市': '101200401',
    '宁德市': '101230301',
    '宁波市': '101210401',
    '安庆市': '101220601',
    '安康市': '101110701',
    '安阳市': '101180201',
    '安顺市': '101260301',
    '定安县': '101310209',
    '定西市': '101160201',
    '宜兰县': '101231001',
    '宜宾市': '101271101',
    '宜昌市': '101200901',
    '宜春市': '101240501',
    '宝鸡市': '101110901',
    '宣城市': '101221401',
    '宿州市': '101220701',
    '宿迁市': '101191301',
    '屏东县': '101340205',
    '屯昌县': '101310210',
    '山南市': '101140301',
    '岳阳市': '101251001',
    '崇左市': '101300201',
    '巴中市': '101270901',
    '巴彦淖尔市': '101080801',
    '巴音郭楞蒙古自治州': '101130601',
    '常州市': '101191101',
    '常德市': '101250601',
    '平凉市': '101160301',
    '平顶山市': '101180501',
    '广元市': '101272101',
    '广安市': '101270801',
    '广州市': '101280101',
    '庆阳市': '101160401',
    '廊坊市': '101090601',
    '延安市': '101110300',
    '延边朝鲜族自治州': '101060301',
    '开封市': '101180801',
    '张家口市': '101090301',
    '张家界市': '101251101',
    '张掖市': '101160701',
    '彰化县': '101340403',
    '徐州市': '101190801',
    '德宏傣族景颇族自治州': '101291501',
    '德州市': '101120401',
    '德阳市': '101272001',
    '忻州市': '101101001',
    '怀化市': '101251201',
    '怒江傈僳族自治州': '101291201',
    '恩施土家族苗族自治州': '101201001',
    '惠州市': '101280301',
    '成都市': '101270101',
    '扬州市': '101190601',
    '承德市': '101090401',
    '抚州市': '101240401',
    '抚顺市': '101070401',
    '拉萨市': '101140101',
    '揭阳市': '101281901',
    '攀枝花市': '101270201',
    '文山壮族苗族自治州': '101290601',
    '文昌市': '101310212',
    '新乡市': '101180301',
    '新余市': '101241001',
    '新北市': '1353E',
    '新星市': '101132501',
    '新竹县': '198CA',
    '新竹市': '101340103',
    '无锡市': '101190201',
    '日喀则市': '101140201',
    '日照市': '101121501',
    '昆明市': '101290101',
    '昆玉市': '101131920',
    '昌吉回族自治州': '101130401',
    '昌江黎族自治县': '101310206',
    '昌都市': '101140501',
    '昭通市': '101291001',
    '晋中市': '101100401',
    '晋城市': '101100601',
    '普洱市': '101290901',
    '景德镇市': '101240801',
    '曲靖市': '101290401',
    '朔州市': '101100901',
    '朝阳市': '101071201',
    '本溪市': '101070501',
    '来宾市': '101300401',
    '杭州市': '101210101',
    '松原市': '101060801',
    '林芝市': '101140401',
    '果洛藏族自治州': '101150501',
    '枣庄市': '101121401',
    '柳州市': '101300301',
    '株洲市': '101250301',
    '桂林市': '101300501',
    '桃园市': '101340102',
    '梅州市': '101280401',
    '梧州市': '101300601',
    '楚雄彝族自治州': '101290801',
    '榆林市': '101110401',
    '武威市': '101160501',
    '武汉市': '101200101',
    '毕节市': '101260701',
    '永州市': '101251401',
    '汉中市': '101110801',
    '汕头市': '101280501',
    '汕尾市': '101282101',
    '江门市': '101281101',
    '池州市': '101221701',
    '沈阳市': '101070101',
    '沧州市': '101090701',
    '河池市': '101301201',
    '河源市': '101281201',
    '泉州市': '101230501',
    '泰安市': '101120801',
    '泰州市': '101191201',
    '泸州市': '101271001',
    '洛阳市': '101180901',
    '济南市': '101120101',
    '济宁市': '101120701',
    '济源市': '101181801',
    '海东市': '101150201',
    '海北藏族自治州': '101150801',
    '海南藏族自治州': '101150401',
    '海口市': '101310101',
    '海西蒙古族藏族自治州': '101150701',
    '淄博市': '101120301',
    '淮北市': '101221201',
    '淮南市': '101220401',
    '淮安市': '101190901',
    '深圳市': '101280601',
    '清远市': '101281301',
    '温州市': '101210701',
    '渭南市': '101110501',
    '湖州市': '101210201',
    '湘潭市': '101250201',
    '湘西土家族苗族自治州': '101251501',
    '湛江市': '101281001',
    '滁州市': '101221101',
    '滨州市': '101121101',
    '漯河市': '101181501',
    '漳州市': '101230601',
    '潍坊市': '101120601',
    '潜江市': '101201701',
    '潮州市': '101281501',
    '澄迈县': '101310204',
    '澎湖县': '753A2',
    '澳门特别行政区': '101330101',
    '濮阳市': '101181301',
    '烟台市': '101120501',
    '焦作市': '101181101',
    '牡丹江市': '101050301',
    '玉林市': '101300901',
    '玉树藏族自治州': '101150601',
    '玉溪市': '101290701',
    '珠海市': '101280701',
    '琼中黎族苗族自治县': '101310208',
    '琼海市': '101310211',
    '甘南藏族自治州': '101161201',
    '甘孜藏族自治州': '101271801',
    '白城市': '101060601',
    '白山市': '101060901',
    '白杨市': '101132601',
    '白沙黎族自治县': '101310207',
    '白银市': '101161301',
    '百色市': '101301001',
    '益阳市': '101250700',
    '盐城市': '101190701',
    '盘锦市': '101071301',
    '眉山市': '101271501',
    '石嘴山市': '101170201',
    '石家庄市': '101090101',
    '石河子市': '101130301',
    '神农架林区': '101201201',
    '福州市': '101230101',
    '秦皇岛市': '101091101',
    '红河哈尼族彝族自治州': '101290301',
    '绍兴市': '101210501',
    '绥化市': '101050501',
    '绵阳市': '101270401',
    '聊城市': '101121701',
    '肇庆市': '101280901',
    '胡杨河市': '101132401',
    '自贡市': '101270301',
    '舟山市': '101211101',
    '芜湖市': '101220301',
    '花莲县': '101340405',
    '苏州市': '101190401',
    '苗栗县': '101340402',
    '茂名市': '101282001',
    '荆州市': '101200801',
    '荆门市': '101201401',
    '莆田市': '101230401',
    '菏泽市': '101121001',
    '萍乡市': '101240901',
    '营口市': '101070801',
    '葫芦岛市': '101071401',
    '蚌埠市': '101220201',
    '衡水市': '101090801',
    '衡阳市': '101250401',
    '衢州市': '101211001',
    '襄阳市': '101200201',
    '西双版纳傣族自治州': '101291601',
    '西宁市': '101150101',
    '西安市': '101110101',
    '许昌市': '101180401',
    '贵港市': '101300801',
    '贵阳市': '101260101',
    '贺州市': '101300701',
    '资阳市': '101271301',
    '赣州市': '101240701',
    '赤峰市': '101080601',
    '辽源市': '101060701',
    '辽阳市': '101071001',
    '达州市': '101270601',
    '运城市': '101100801',
    '连云港市': '101191001',
    '迪庆藏族自治州': '101291301',
    '通化市': '101060501',
    '通辽市': '101080501',
    '遂宁市': '101270701',
    '遵义市': '101260201',
    '邢台市': '101090901',
    '那曲市': '101140601',
    '邯郸市': '101091001',
    '邵阳市': '101250901',
    '郑州市': '101180101',
    '郴州市': '101250501',
    '鄂尔多斯市': '101080701',
    '鄂州市': '101200301',
    '酒泉市': '101160801',
    '重庆市': '101040100',
    '金华市': '101210901',
    '金昌市': '101160601',
    '钦州市': '101301101',
    '铁岭市': '101071101',
    '铁门关市': '101131901',
    '铜仁市': '101260601',
    '铜川市': '101111001',
    '铜陵市': '101221301',
    '银川市': '101170101',
    '锡林郭勒盟': '101080901',
    '锦州市': '101070701',
    '镇江市': '101190301',
    '长春市': '101060101',
    '长沙市': '101250101',
    '长治市': '101100501',
    '阜新市': '101070901',
    '阜阳市': '101220801',
    '防城港市': '101301401',
    '阳江市': '101281801',
    '阳泉市': '101100301',
    '阿克苏地区': '101130801',
    '阿勒泰地区': '101131401',
    '阿坝藏族羌族自治州': '101271901',
    '阿拉善盟': '101081201',
    '阿拉尔市': '101130701',
    '阿里地区': '101140701',
    '陇南市': '101161001',
    '陵水黎族自治县': '101310216',
    '随州市': '101201301',
    '雄安新区': '101091201',
    '雅安市': '101271701',
    '青岛市': '101120201',
    '鞍山市': '101070301',
    '韶关市': '101280201',
    '香港特别行政区': '101320101',
    '马鞍山市': '101220501',
    '驻马店市': '101181601',
    '高雄市': '101340201',
    '鸡西市': '101051101',
    '鹤壁市': '101181201',
    '鹤岗市': '101051201',
    '鹰潭市': '101241101',
    '黄冈市': '101200501',
    '黄南藏族自治州': '101150301',
    '黄山市': '101221001',
    '黄石市': '101200601',
    '黑河市': '101050601',
    '黔东南苗族侗族自治州': '101260501',
    '黔南布依族苗族自治州': '101260401',
    '黔西南布依族苗族自治州': '101260901',
    '齐齐哈尔市': '101050201',
    '龙岩市': '101230701'
};

// Store
const ai = definedPerson();

// --- DATA ---
const weatherForecast = ref([]);

// 天气图标映射
const weatherIconMap = {
	'晴': '☀️',
	'多云': '⛅',
	'阴': '☁️',
	'阵雨': '🌦️',
	'雷阵雨': '⛈️',
	'小雨': '🌧️',
	'中雨': '🌧️',
	'大雨': '🌧️',
	'暴雨': '🌧️',
	'雪': '❄️',
	'雾': '🌫️',
	'霾': '🌫️'
};

// --- COMPUTED ---
const isProvinceView = computed(() => currentMapName.value !== 'china');
const mapTitle = computed(() => isProvinceView.value ? `${currentRegionName.value}价格分布图` : '全国各省价格分布图');
const rankingTitle = computed(() => `明日${currentRegionName.value}价格预测`);
const rankingRegionHeader = computed(() => isProvinceView.value ? '城市' : '省份');

const rankingFooterText = computed(() =>
	`仅显示${currentRegionName.value}${selectedVegetable.value}价格最高的前10个${isProvinceView.value ? '城市' : '省份'}`
);

// 省份名称到代码的映射函数
const getProvinceCode = (provinceName) => {
  const provinceMap = {
    '北京市': '110000',
    '天津市': '120000',
    '河北省': '130000',
    '山西省': '140000',
    '内蒙古自治区': '150000',
    '辽宁省': '210000',
    '吉林省': '220000',
    '黑龙江省': '230000',
    '上海市': '310000',
    '江苏省': '320000',
    '浙江省': '330000',
    '安徽省': '340000',
    '福建省': '350000',
    '江西省': '360000',
    '山东省': '370000',
    '河南省': '410000',
    '湖北省': '420000',
    '湖南省': '430000',
    '广东省': '440000',
    '广西壮族自治区': '450000',
    '海南省': '460000',
    '重庆市': '500000',
    '四川省': '510000',
    '贵州省': '520000',
    '云南省': '530000',
    '西藏自治区': '540000',
    '陕西省': '610000',
    '甘肃省': '620000',
    '青海省': '630000',
    '宁夏回族自治区': '640000',
    '新疆维吾尔自治区': '650000',
    '台湾省': '710000',
    '香港特别行政区': '810000',
    '澳门特别行政区': '820000'
  };
  
  return provinceMap[provinceName] || '';
};

// 蔬菜价格数据计算属性
const vegetablePrices = computed(() => {
  if (!provincePriceData.value.length) return {};
  
  const result = {};
  
  // 按蔬菜品种分组
  provincePriceData.value.forEach(item => {
    const vegetable = item.varietyName;
    if (!result[vegetable]) {
      result[vegetable] = [];
    }
    
    // 查找是否已存在该省份的数据
    const existingProvince = result[vegetable].find(prov => prov.name === item.provinceName);
    
    if (existingProvince) {
      // 如果已存在，更新价格（这里使用中间价）
      existingProvince.value = parseFloat(item.middlePrice);
    } else {
      // 如果不存在，添加新省份
      result[vegetable].push({
        name: item.provinceName,
        value: parseFloat(item.middlePrice)
      });
    }
  });
  
  return result;
});

const provinceVegetablePrices = computed(() => {
  if (!cityPriceData.value.length) return {};
  
  const result = {};
  
  // 按省份代码和蔬菜品种分组
  cityPriceData.value.forEach(item => {
    // 将省份名称转换为省份代码
    const provinceCode = getProvinceCode(item.provinceName);
    const vegetable = item.varietyName;
    
    if (!result[provinceCode]) {
      result[provinceCode] = {};
    }
    
    if (!result[provinceCode][vegetable]) {
      result[provinceCode][vegetable] = [];
    }
    
    // 添加城市价格数据
    result[provinceCode][vegetable].push({
      name: item.areaName,
      value: parseFloat(item.middlePrice)
    });
  });
  
  return result;
});

const top10Prices = computed(() => {
  let prices;
  
  if (!isProvinceView.value) {
    // 全国视图：使用省均价数据
    prices = vegetablePrices.value[selectedVegetable.value] || [];
  } else {
    // 省份视图：使用市均价数据，过滤当前省份的城市
    const currentProvinceName = currentRegionName.value;
    const currentProvinceCode = getProvinceCode(currentProvinceName);
    prices = provinceVegetablePrices.value[currentProvinceCode]?.[selectedVegetable.value] || [];
  }
  
  // 返回排序后的副本，不改变原始数据
  return [...prices].sort((a, b) => b.value - a.value).slice(0, 10);
});

// --- METHODS ---
const getRankClass = (index) => (index < 3 ? `top-${index + 1}` : '');

// 获取天气图标
const getWeatherIcon = (weatherText) => {
	return weatherIconMap[weatherText] || '🌤️';
};

// 格式化日期显示
const formatDate = (dateString) => {
	const date = new Date(dateString);
	return `${date.getMonth() + 1}月${date.getDate()}日`;
};

// 格式化星期显示
const formatDay = (dateString) => {
	const date = new Date(dateString);
	const today = new Date();
	const tomorrow = new Date(today);
	tomorrow.setDate(today.getDate() + 1);
	
	if (date.toDateString() === today.toDateString()) {
		return '今天';
	} else if (date.toDateString() === tomorrow.toDateString()) {
		return '明天';
	} else {
		const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return days[date.getDay()];
	}
};

// 加载农产品分类数据
const loadProductOptions = async () => {
	try {
		const response = await selectKind({}, '/vegetable/type2/all');
		if (response.data.code == '0') {
			// 重组数据结构为 el-select 所需的分组格式
			const groupedData = {};
			
			response.data.data.forEach(item => {
				if (!groupedData[item.oneLevel]) {
					groupedData[item.oneLevel] = {
						oneLevel: item.oneLevel,
						items: []
					};
				}
				
				groupedData[item.oneLevel].items.push(item);
			});
			
			productOptions.value = Object.values(groupedData);
		} else {
			console.error('获取农产品分类数据失败:', response.data.msg);
		}
	} catch (error) {
		console.error('获取农产品分类数据失败:', error);
	}
};

import { get7DayWeather } from '/src/api/requestFuntion.js';

// 修改fetchWeatherData方法
const fetchWeatherData = async (cityName) => {
    if (!cityName) {
        ElMessage.warning('请选择城市');
        return;
    }
    
    // 获取城市代码
    const cityCode = cityCodeMap[cityName];
    if (!cityCode) {
        ElMessage.warning(`未找到城市 ${cityName} 的代码`);
        return;
    }
    
    console.log(`准备获取天气数据，城市: ${cityName}, 代码: ${cityCode}`);
    
    loading.value = true;
    
    try {
        const response = await get7DayWeather(cityCode);
        console.log('axios完整响应:', response);
        
        const weatherData = response.data;
        console.log('天气API响应数据:', weatherData);
        
        // 详细检查数据结构
        if (weatherData) {
            console.log('weatherData.code:', weatherData.code);
            console.log('weatherData.daily:', weatherData.daily);
            console.log('daily类型:', typeof weatherData.daily);
            console.log('daily是数组:', Array.isArray(weatherData.daily));
            
            if (weatherData.daily && Array.isArray(weatherData.daily)) {
                console.log('daily长度:', weatherData.daily.length);
                if (weatherData.daily.length > 0) {
                    console.log('第一条数据:', weatherData.daily[0]);
                }
            }
        }
        
        if (weatherData && weatherData.code === "200") {
            if (weatherData.daily && Array.isArray(weatherData.daily)) {
                weatherForecast.value = weatherData.daily;
                console.log(`成功设置天气数据，共${weatherData.daily.length}天`);
                ElMessage.success('天气数据获取成功');
            } else {
                console.warn('天气数据中daily字段不存在或不是数组:', weatherData);
                ElMessage.warning('天气数据格式异常');
            }
        } else {
            console.error('天气API返回错误:', weatherData);
            ElMessage.error(`天气数据获取失败: ${weatherData?.msg || '未知错误'}`);
        }
    } catch (error) {
        console.error('获取天气数据失败:', error);
        console.error('错误详情:', error.response);
        ElMessage.error('天气数据获取失败，请检查网络连接');
    } finally {
        loading.value = false;
    }
};

// 解析CSV数据的函数
const parseCSV = (csvText) => {
  const lines = csvText.split('\n');
  const headers = lines[0].split(',').map(header => header.trim());
  
  const result = [];
  
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;
    
    const values = [];
    let inQuotes = false;
    let currentValue = '';
    
    for (let j = 0; j < line.length; j++) {
      const char = line[j];
      
      if (char === '"') {
        inQuotes = !inQuotes;
      } else if (char === ',' && !inQuotes) {
        values.push(currentValue.trim());
        currentValue = '';
      } else {
        currentValue += char;
      }
    }
    
    values.push(currentValue.trim());
    
    const item = {};
    headers.forEach((header, index) => {
      item[header] = values[index] || '';
    });
    
    result.push(item);
  }
  
  return result;
};

// 加载CSV数据的函数
const loadCSVData = async () => {
  try {
    // 加载市均价数据
    const cityResponse = await fetch('/data/city_prices.csv'); // 假设文件放在public/data目录下
    const cityCSVText = await cityResponse.text();
    cityPriceData.value = parseCSV(cityCSVText);
    
    // 加载省均价数据
    const provinceResponse = await fetch('/data/province_prices.csv'); // 假设文件放在public/data目录下
    const provinceCSVText = await provinceResponse.text();
    provincePriceData.value = parseCSV(provinceCSVText);
    
    console.log('CSV数据加载成功');
    console.log('市均价数据:', cityPriceData.value);
    console.log('省均价数据:', provincePriceData.value);
    
    // 数据加载完成后更新地图
    updateMap();
  } catch (error) {
    console.error('加载CSV数据失败:', error);
    ElMessage.error('加载价格数据失败');
  }
};

const updateMap = () => {
	if (!mapChart) return;
	
	let data;
	
	if (!isProvinceView.value) {
		// 全国视图：使用省均价数据
		data = vegetablePrices.value[selectedVegetable.value] || [];
	} else {
		// 省份视图：使用市均价数据，过滤当前省份的城市
		const currentProvinceName = currentRegionName.value;
		const currentProvinceCode = getProvinceCode(currentProvinceName);
		data = provinceVegetablePrices.value[currentProvinceCode]?.[selectedVegetable.value] || [];
	}

  // 计算价格范围
  const prices = data.map(item => item.value).filter(val => !isNaN(val));
  const minPrice = prices.length ? Math.min(...prices) : 0;
  const maxPrice = prices.length ? Math.max(...prices) : 10;

	const mapOption = {
		tooltip: {
			trigger: 'item',
			formatter: (params) => `${params.name}<br/>${selectedVegetable.value}价格: ${params.value ? params.value.toFixed(2) + '元/斤' : '无数据'}`
		},
		visualMap: {
			min: minPrice,
      max: maxPrice,
      text: ['高价', '低价'],
			realtime: false,
			calculable: true,
			inRange: { color: [   '#fc6243','#fcb943','#fcf843','#43fc79','#43fce0','#37b4fd','#3137fd'] },
			left: 'left',
			top: 'bottom',
			textStyle: { color: '#333' }
		},
		series: [{
			name: `${selectedVegetable.value}价格`,
			type: 'map',
			map: currentMapName.value,
			roam: true,
			zoom: currentMapZoom,
			emphasis: {
				label: { show: true, color: '#fff', backgroundColor: 'rgba(0,0,0,0.7)', padding: [4, 6], borderRadius: 4 },
				itemStyle: { areaColor: '#ff7f50' }
			},
			data: data,
			nameMap: isProvinceView.value ? {} : { '内蒙古': '内蒙古自治区', '新疆': '新疆维吾尔自治区', '西藏': '西藏自治区', '广西': '广西壮族自治区', '宁夏': '宁夏回族自治区', '香港': '香港特别行政区', '澳门': '澳门特别行政区' }
		}]
	};
	mapChart.setOption(mapOption, true);
};

const loadProvinceMap = async (provinceCode, provinceName) => {
	try {
		mapChart.showLoading();
		const response = await fetch(`https://geo.datav.aliyun.com/areas_v3/bound/${provinceCode}_full.json`);
		const provinceGeoJSON = await response.json();
		echarts.registerMap(provinceCode, provinceGeoJSON);

		currentMapName.value = provinceCode;
		currentRegionName.value = provinceName;
		currentMapZoom = 1.0;
		updateMap();
	} catch (error) {
		console.error('加载省份地图数据失败:', error);
		alert('省份地图数据加载失败，请检查网络连接');
	} finally {
		mapChart.hideLoading();
	}
};

const backToNationalMap = () => {
	currentMapName.value = 'china';
	currentRegionName.value = '全国';
	currentMapZoom = 1.2;
	updateMap();
};

const initMap = async () => {
	try {
		const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json');
		const chinaGeoJSON = await response.json();
		echarts.registerMap('china', chinaGeoJSON);

		mapChart = echarts.init(mapContainerRef.value);
		updateMap();

		mapChart.on('georoam', (params) => {
			if (params.zoom) {
				const option = mapChart.getOption();
				currentMapZoom = option.series[0].zoom;
			}
		});

		mapChart.on('click', (params) => {
			if (currentMapName.value === 'china') {
				const provinceName = params.name;
				const provinceCode = getProvinceCode(provinceName);
				if (provinceCode) {
					loadProvinceMap(provinceCode, provinceName);
				}
			}
		});

	} catch (error) {
		console.error('加载中国地图数据失败:', error);
		alert('中国地图数据加载失败，请检查网络连接');
	}
};

// 地图控制
const zoomIn = () => {
	currentMapZoom = Math.min(currentMapZoom * 1.2, 5);
	mapChart.setOption({ series: [{ zoom: currentMapZoom }] });
};
const zoomOut = () => {
	currentMapZoom = Math.max(currentMapZoom * 0.8, 0.6);
	mapChart.setOption({ series: [{ zoom: currentMapZoom }] });
};
const resetView = () => {
	currentMapZoom = isProvinceView.value ? 1.0 : 1.2;
	mapChart.setOption({ series: [{ zoom: currentMapZoom, center: null }] });
};

// 其他处理函数
const handleSearch = () => {
	fetchWeatherData(city.value);
};
const handleRankItemClick = (item) => {
  if (isProvinceView.value) {
    // 在省份视图中点击城市，可以跳转到该城市的详细信息
    ElMessage.info(`您选择了: ${item.name} - ${selectedVegetable.value} 价格: ${item.value.toFixed(2)}元/斤`);
  } else {
    // 在全国视图中点击省份，跳转到该省份的详细视图
    const provinceCode = getProvinceCode(item.name);
    if (provinceCode) {
      loadProvinceMap(provinceCode, item.name);
    }
  }
};

// 修复省份变化处理函数
const handleProvinceChange = (value) => {
	ai.province = value;
	// 更新城市选项
	cityOptions.value = provinceCityMap[value] || [];
	// 重置城市选择
	if (cityOptions.value.length > 0) {
		city.value = cityOptions.value[0];
	} else {
		city.value = '';
	}
};

// --- WATCHERS ---
watch(selectedVegetable, updateMap);

// --- LIFECYCLE HOOKS ---
onMounted(() => {
	initMap();
	window.addEventListener('resize', () => mapChart?.resize());
	
	// 使用本地的省份数据，不再调用API
	provinceOptions.value = Object.keys(provinceCityMap);
	
	// 设置默认省份和城市
	if (provinceOptions.value.length > 0) {
		province.value = '北京市'; // 设置默认省份
		cityOptions.value = provinceCityMap[province.value] || [];
		if (cityOptions.value.length > 0) {
			city.value = cityOptions.value[0];
		}
	}
	
	// 加载农产品分类数据
	loadProductOptions();
	
	// 加载CSV价格数据
	loadCSVData();
});

onUnmounted(() => {
	window.removeEventListener('resize', () => mapChart?.resize());
	mapChart?.dispose();
});
</script>