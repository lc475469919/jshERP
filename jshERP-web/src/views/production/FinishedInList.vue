<script>
  import OtherInList from '@/views/bill/OtherInList'

  export default {
    name: 'ProductionFinishedInList',
    extends: OtherInList,
    data() {
      return {
        queryParam: {
          number: '',
          materialParam: '',
          type: '入库',
          subType: '成品入库',
          organId: undefined,
          depotId: undefined,
          creator: undefined,
          linkNumber: '',
          status: undefined,
          remark: ''
        },
        billTypeName: '成品入库',
        billSubType: '成品入库',
        helpText: '用于生产任务完工后的成品入库。',
        urlPath: '/production/finished_in',
        defDataIndex:['action','organName','number','linkNumber','materialsList','operTimeStr','userName','materialCount','totalPrice','status']
      }
    },
    methods: {
      prepareBillModal() {
        this.$refs.modalForm.billTypeName = this.billTypeName
        this.$refs.modalForm.billSubType = this.billSubType
        this.$refs.modalForm.productionLinkMode = true
      }
    }
  }
</script>
