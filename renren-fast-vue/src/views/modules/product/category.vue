<script setup>

</script>

<template>
  <el-tree :data="data" :props="defaultProps" @node-click="handleNodeClick"></el-tree>
</template>

<style scoped lang="scss">

</style>

<script>
export default {
  data() {


  },
  methods: {
     getMenu(){
       this.dataListLoading = true
       this.$http({
         url: this.$http.adornUrl('/product/category/list/tree'),
         method: 'get',

       }).then(({data}) => {
         if (data && data.code === 0) {
           this.dataList = data.page.list
           this.totalPage = data.page.totalCount
         } else {
           this.dataList = []
           this.totalPage = 0
         }
         this.dataListLoading = false
        })
     }
  },
  created() {
    this.getMenu()
  }
};
</script>
