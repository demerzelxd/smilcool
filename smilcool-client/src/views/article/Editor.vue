<template>
  <div class="container">
    <!-- 标题 -->
    <div class="article-info-title">
      <Input v-model="articleAddForm.title" placeholder="输入文章标题" size="large" style="z-index: 1">
        <template #append>
          <Button icon="md-create" @click="articleInfoModal.show = true">提交</Button>
        </template>
      </Input>
    </div>
    <!-- 标题 END -->
    <!-- 文章信息模态框 -->
    <Modal v-model="articleInfoModal.show" title="文章信息" :mask-closable="false" :width="600" scrollable>
      <Row>
        <iCol span="8">
          <sui-image :src="articleAddForm.cover" size="medium" rounded/>
          <Upload action="//jsonplaceholder.typicode.com/posts/">
            <Button icon="ios-cloud-upload-outline">上传文章封面</Button>
          </Upload>
        </iCol>
        <iCol span="16">
          <div class="article-info-row">
            <Select v-model="articleAddForm.articleCategory" placeholder="请选择文章类别" size="large">
              <Option v-for="item in articleCategory" :key="item.name" :value="item.name" :label="item.name"/>
            </Select>
          </div>
          <div class="article-info-row">
            <Input type="textarea" v-model="articleAddForm.brief" size="large" :autosize="{minRows: 5,maxRows: 10}"
                   placeholder="请输入文章简介"/>
          </div>
          <div class="article-info-row">
            <Input class="article-info-tag" prefix="ios-bookmark-outline" v-model="tag" placeholder="添加标签"
                   @on-enter="addTag"/>
            <Tag class="article-info-tag" v-for="(item, index) in tags" :key="index" :name="item"
                 @on-close="removeTag" closable type="dot" color="primary">
              {{item}}
            </Tag>
          </div>
        </iCol>
      </Row>
      <template #footer>
        <Button type="text" @click="articleInfoModal.show = false">取消</Button>
        <Button type="primary" @click="submitArticle" :loading="articleInfoModal.loading">确认提交</Button>
      </template>
    </Modal>
    <!-- 文章信息模态框 END -->
    <!-- 编辑器 -->
    <mavon-editor class="editor" ref=editor v-model="articleAddForm.markdownContent"
                  @change="contentChange" @imgAdd="imgAdd" :boxShadow="false"/>
    <!-- 编辑器 END -->
  </div>
</template>

<script>
export default {
  name: 'Editor',
  data() {
    return {
      articleInfoModal: {
        show: false,
        loading: false
      },
      tag: '',
      tags: [],
      articleCategory: [{
        name: '测试类别',
        code: '-1'
      }],
      articleAddForm: {
        articleCategory: '',
        title: '',
        brief: '',
        cover: 'http://img.angus-liu.cn/avatar/avatar01.jpg',
        tags: '',
        markdownContent: '',
        htmlContent: ''
      }
    };
  },
  methods: {
    // 获取文章类别
    getArticleCategory() {
      this.$axios.get('/api/dic/item', { dicTypeCode: 'article-category' })
        .then(res => {
          let result = res.data;
          this.articleCategory = result.data;
        })
    },
    // 编辑器内容变化
    contentChange(value, render) {
      this.articleAddForm.markdownContent = value;
      this.articleAddForm.htmlContent = render;
    },
    // 保存草稿
    saveArticle(value, render) {
      console.log('保存草稿');
    },
    // 添加图片
    imgAdd(pos, img){
      // 第一步.将图片上传到服务器.
      let formData = new FormData();
      formData.append('type', 'img');
      formData.append('file', img);
      this.$axios.post('/api/local-storage/upload', formData)
        .then(res => {
        let result = res.data;
        this.$refs.editor.$img2Url(pos, result.data.url);
      })
    },
    // 添加标签
    addTag() {
      this.tags.push(this.tag);
      this.tag = '';
    },
    removeTag(event, name) {
      const index = this.tags.indexOf(name);
      this.tags.splice(index, 1);
    },
    submitArticle() {
      this.articleInfoModal.loading = true;
      this.articleAddForm.tags = JSON.stringify(this.tags);
      this.$axios.post('/api/article', this.articleAddForm)
        .then(res => {
          this.articleInfoModal.show = false;
          this.articleInfoModal.loading = false;
          this.$Notice.success({ title: 'Bingo', desc: '发布成功' });
          this.$router.push('/');
        })
        .catch(error => {
          this.articleInfoModal.loading = false;
        });
    }
  },
  mounted() {
    this.getArticleCategory();
  }
};
</script>

<style lang="less" scoped>
.container {
  height: 100%;
  width: 1260px;
  margin: 20px auto;

  .article-info-title {
    margin-bottom: 7px;
  }

  .editor {
    height: 780px;
    z-index: 1;
  }

  .fullscreen {
    height: 100%;
    z-index: 100;
  }
}

.article-info-row {
  margin-bottom: 10px;

  .article-info-tag {
    margin-bottom: 5px;
  }
}
</style>
