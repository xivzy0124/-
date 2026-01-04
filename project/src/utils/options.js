import {reactive} from 'vue'
export const yesterdayProvincePrice = reactive(
[
  {
    label: '蔬菜',
    options: [
      {
        value: '大白菜',
        label: '大白菜',
      },
      {
        value: '土豆',
        label: '土豆',
      },
	  {
	    value: '大葱',
	    label: '大葱',
	  },
	  {
	    value: '胡萝卜',
	    label: '胡萝卜',
	  }
    ],
  },
  {
    label: '肉类',
    options: [
      {
        value: '牛肉',
        label: '牛肉',
      },
      {
        value: '猪肉(白条猪)',
        label: '猪肉',
      },
      {
        value: '羊肉',
        label: '羊肉',
      }
    ]
  }
]
)