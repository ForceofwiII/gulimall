<script setup>
</script>

<template>
  <div>
    <el-button type="danger"   @click="batchDelete">批量删除</el-button>
  <el-tree :data="menus" :props="defaultProps" @node-click="handleNodeClick"
           :expand-on-click-node="false" show-checkbox
           node-key="catId" :default-expanded-keys="expandedKey"  ref="menuTree">
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

          type="text"
          size="mini"
          @click="edit(data)">
          edit
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

  <el-dialog   title="提示"
               :visible.sync="dialogVisible"
               width="30%"   :close-on-click-modal="false">

    <el-form :model="category">
      <el-form-item label="分类名称" >
        <el-input v-model="category.name" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="分类图标" >
        <el-input v-model="category.icon" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label="分类数量" >
        <el-input v-model="category.product_unit" autocomplete="off"></el-input>
      </el-form-item>

    </el-form>
    <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">取 消</el-button>
    <el-button type="primary" @click="addCategory">确 定</el-button>
  </span>
  </el-dialog>
  </div>
</template>

<style scoped lang="scss">
</style>

<script>
export default {
  data() {
    return {
      category:{
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        catId: null,
        icon: null,
        product_unit: null
      },
      dialogVisible: false,
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
      // 重置 category 对象的所有属性
      this.category = {
        name: "",
        parentCid: data.catId,
        catLevel: data.catLevel * 1 + 1,
        showStatus: 1,
        sort: 0,
        catId: null,
        icon: null,
        product_unit: null
      };
      this.dialogVisible = true;

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
    },
    //添加商品分类
    addCategory(){

      this.$http({
        url: this.$http.adornUrl('/product/category/save'),
        method: 'post',
        data: this.$http.adornData(this.category, false)
      }).then(({ data }) => {
        this.$message({
          message: '操作成功',
          type: 'success'
        });
        this.dialogVisible = false;

        this.getMenu();
        this.expandedKey = [this.category.parentCid];

      });


      },
    edit(data){
      this.dialogVisible = true;

      this.$http({
        url: this.$http.adornUrl(`/product/category/info/+${data.catId}`),
        method: 'get',
      }).then(({ data }) => {
        this.category.name = data.data.name;
        this.category.icon = data.data.icon;
        this.category.product_unit = data.data.product_unit;
        this.category.catId = data.data.catId;
        this.category.parentCid = data.data.parentCid;
        this.category.catLevel = data.data.catLevel;
        this.category.showStatus = data.data.showStatus;
        this.category.sort = data.data.sort;


      });




    },
    batchDelete(){
      console.log("我被点击了")
      //获得menuTree组件的选中的内容
      let checkedKeys = this.$refs.menuTree.getCheckedKeys();
      console.log(checkedKeys)
     let id=[];
     for(let i=0;i<checkedKeys.length;i++){
        id.push(checkedKeys[i]);
      }
      console.log(id)
      this.$confirm('此操作将永久删除该商品, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/category/delete'),
          method: 'post',
          data: this.$http.adornData(id, false)
        }).then(({ data }) => {
          this.$message({
            message: '删除成功',
            type: 'success'
          });

          this.getMenu();

        });
      });

    }

  },
  created() {
    this.getMenu();
  }
};
</script>
