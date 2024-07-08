<script setup>
</script>

<template>
  <el-tree :data="menus" :props="defaultProps" @node-click="handleNodeClick"
           :expand-on-click-node="false" show-checkbox
           node-key="catId" :default-expanded-keys="expandedKey">
    <span class="custom-tree-node" slot-scope="{ node, data }">
      <span>{{ node.label }}</span>
      <span>
        <el-button
          v-if="node.level <= 2"
          type="text"
          size="mini"
          @click="() => append(data)">
          Append
        </el-button>
        <el-button
          v-if="node.childNodes.length == 0"
          type="text"
          size="mini"
          @click="() => remove(node, data)">
          Delete
        </el-button>
      </span>
    </span>
  </el-tree>
</template>

<style scoped lang="scss">
</style>

<script>
export default {
  data() {
    return {
      menus: [],
      expandedKey: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  methods: {
    getMenu() {
      this.dataListLoading = true;
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get',
      }).then(({ data }) => {
        this.menus = data.data;
      });
    },
    append(data) {
      console.log("我被点击了");
    },
    remove(node, data) {
      var ids = [data.catId];
      this.$confirm('此操作将永久删除该商品, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'post',
          data: this.$http.adornData(ids, false)
        }).then(({ data }) => {
          this.$message({
            message: '删除成功',
            type: 'success'
          });

          this.getMenu();

            this.expandedKey = [node.parent.data.catId];

        });
      });
    }
  },
  created() {
    this.getMenu();
  }
};
</script>
