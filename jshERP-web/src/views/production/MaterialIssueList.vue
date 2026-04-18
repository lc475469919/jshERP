<script>
  import OtherOutList from '@/views/bill/OtherOutList'

  export default {
    name: 'ProductionMaterialIssueList',
    extends: OtherOutList,
    data() {
      return {
        queryParam: {
          number: '',
          materialParam: '',
          type: '出库',
          subType: '生产领料',
          organId: undefined,
          depotId: undefined,
          creator: undefined,
          linkNumber: '',
          status: undefined,
          remark: ''
        },
        billTypeName: '生产领料',
        billSubType: '生产领料',
        helpText: '用于生产任务的原料领用出库。',
        urlPath: '/production/material_issue',
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
