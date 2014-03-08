(function() {
  var AppRouter, Drawer, Group, Project, TaskFolder, TaskItem, Tasks, log,
    __slice = [].slice,
    __hasProp = {}.hasOwnProperty,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  log = function() {
    var args;
    args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (console.log != null) {
      return console.log.apply(console, args);
    }
  };

  $(".options dt, .users dt").live("click", function(e) {
    e.preventDefault();
    if ($(e.target).parent().hasClass("opened")) {
      $(e.target).parent().removeClass("opened");
    } else {
      $(e.target).parent().addClass("opened");
      $(document).one("click", function() {
        return $(e.target).parent().removeClass("opened");
      });
    }
    return false;
  });

  $.fn.editInPlace = function() {
    var method, options;
    method = arguments[0],
    options = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    return this.each(function() {
      var methods;
      methods = {
        init: function(options) {
          var cancel, valid,
            _this = this;
          valid = function(e) {
            var newValue;
            newValue = _this.input.val();
            return options.onChange.call(options.context, newValue);
          };
          cancel = function(e) {
            _this.el.show();
            return _this.input.hide();
          };
          this.el = $(this).dblclick(methods.edit);
          return this.input = $("<input type='text' />").insertBefore(this.el).keyup(function(e) {
            switch (e.keyCode) {
              case 13:
                return $(this).blur();
              case 27:
                return cancel(e);
            }
          }).blur(valid).hide();
        },
        edit: function() {
          this.input.val(this.el.text()).show().focus().select();
          return this.el.hide();
        },
        close: function(newName) {
          this.el.text(newName).show();
          return this.input.hide();
        }
      };
      if (methods[method]) {
        return methods[method].apply(this, options);
      } else if (typeof method === 'object') {
        return methods.init.call(this, method);
      } else {
        return $.error("Method " + method + " does not exist.");
      }
    });
  };


  AppRouter = (function(Router) {
      function load(url){
          $('#top_loading').show()
          $('#main').load(url,function(){
              $('#top_loading').hide()
          });
      }
      var router = Router.extend({
          routes:{
              "documents": "documents",
              "fileUpload":"fileUpload",
              "document/:id": "document",
              "page/:id": "page"
          },
          documents:function(){
              return load("/documents");

          },
          fileUpload:function(){
              return load("/fileUpload");
          },
          document:function(id){
              return load("/document/"+id);
          },
          page:function(id){
              return load('/page/'+id);
          }
      });

    return router;

  })(Backbone.Router);


  //Modal box
  var Modal = (function(){
      var tpl = '<div class="modal fade modal-overflow in" tabindex="-1" data-width="760"  aria-hidden="false">'+
                        '<div class="modal-header">'+
                            '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'+
                            '<h3 class="J_title"></h3>'+
                        '</div>'+
                        '<div class="modal-body"></div>'+
                        '<div class="modal-footer">'+
                            '<button type="button" data-dismiss="modal" class="btn">关闭</button>'+
                            '<button type="button" class="btn btn-primary J_save">保存</button>'+
                        '</div>'+
                    '</div>';
      var M = function(contentHtml,title){
          var self = this;
          var el = this.el = $(tpl);
          el.find('.modal-body').append(contentHtml)
          el.find('.J_title').text(title);
          el.modal();


          el.on('hide',function(){
              self.trigger('hide');
          });

          el.find('.J_save').click(function(){
              self.trigger('save');
          });
      };
      M.prototype.hide = function(){
          this.el.data('modal').hide();
      };
      _.extend(M.prototype,Backbone.Events);
      return M;

  })();

  var Question = (function(){
      var LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYX";
      var TYPES = {
          blank:"填空",
          choice:"单选",
          multichoice:"多选"
      };
      var getIndex = function(i){
          return LETTERS.charAt(i);
      };
      var TPL = '<div class="question_editor"><table><tbody>'+
          '<tr><td>问题类型:</td><td><select class="J_type"><%_.each(types,function(value,key){%><option value="<%=key%>"><%=value%></option><%});%></select></td></tr>'+
          '<tr class="J_options_restrict" style="display:<%if(type=="blank"||type==""){}else{%>none<%}%>;">'+
            '<td>问题约束:</td><td>'+
            '<select class="J_restrict"><%restricts.forEach(function(r){%>'+
                '<option value="<%=r.type%>" <%if(desc.restrict && desc.restrict==r.type){%>selected<%}%>><%=r.name%></option>'+
            '<%});%></select></td>'+
          '</tr>'+
          '<tr>'+
            '<td>问题:</td><td><textarea placeholder="问题描述" class="J_content"><%=desc.content%></textarea></td>'+
          '</tr>'+
          '</tbody></table>'+
          '<div class="J_option_area options_area" style="display:<%if(type=="blank" || type==""){%>none<%}%>;"><p><button type="button" class="btn btn-primary J_option_add">+选项</button></p>'+
          '<ol class="J_options options_list">'+
          '<%if(desc.options){desc.options.forEach(function(item,i){%>'+
          '<li><input type="text" value="<%=item%>"><a onclick="$(this).parent().remove();" href="javascript:;">x</a></li>'+
          '<%})}%>'+
          '</ol></div>'+
          '</div>';
      var DISPLAY_TPL = '<dl class="questions well"><dt><%=desc.content%></dt>'+
          '<%if(type!="blank" && desc.options){desc.options.forEach(function(item,i){%>'+
          '<dd><%=(i+1)%>.<%=item%></dd>'+
          '<%})}%>'+
          '</dl>';
      var Model = Backbone.Model.extend({
          defaults:{
              types:TYPES
          },
          url:function(){
              return '/question/'+this.get('id');
          },
          parse:function(res){
              if(res.code==200){
                  var data = res.data;
                  var desc = {};
                  try{
                      desc = JSON.parse(data.description);
                  }catch(e){ }
                  data.desc = desc;
                  return data;
              }else {
                  return {
                      desc:{},
                      type:""
                  };
              }
          }
      });
      var View = Backbone.View.extend({
          template:_.template(TPL),
          render:function(){
              var html = this.template(this.model.attributes);
              this.modal = new Modal(html,'编辑问题');
              this.modal.el.find('.J_type').val(this.model.get('type'));
          }
      });
      var DisplayView = Backbone.View.extend({
          template:_.template(DISPLAY_TPL),
          render:function(){
              var html = this.template(this.model.attributes);
              return html;
          }
      });

      var Controller = function(attrs,btn){
          var self = this;
          this.model = new Model(attrs);
          this.view = new View({
              model:this.model
          });
          this.displayView = new DisplayView({
              model:this.model
          });
          this.btn = btn;

          this.model.on('change',function(){
              if(btn){
                  btn.parent().next().remove();
                  btn.parent().after(self.displayView.render());
              }
          });
      };
      Controller.prototype.edit = function(){
          var self = this;
          this.model.fetch();
          this.model.bind('sync',function(){
              self.view.render();
              self.view.modal.bind('save',function(){
                  self.save();
              });

              var el = self.view.modal.el;
              el.find('.J_option_add').click(function(){
                  self.addOption();
              });
              el.find('.J_type').change(function(){
                  self.changeType($(this).val());
              });
              if(self.model.get("type")==="blank"){
                el.find(".J_option_area").hide();
              }
          });
      };
      Controller.prototype.save = function(){
          var self = this;
          var data = this.getData();
          $.ajax({
              url:"/questionsave",
              data:data,
              type:"POST",
              dataType:"json",
              success:function(res){
                  if(res.code===200){
                      data.id = res.data.id;
                  }
                  self.view.modal.hide();
                  self.model.set(data);
                  self.trigger('save',self.model);
              }
          });
      };
      Controller.prototype.addOption = function(){
          var options = this.view.modal.el.find('.J_options');
          $('<li><input type="text" ><a onclick="$(this).parent().remove();" href="javascript:;">x</a></li>')
          .appendTo(options).hide().fadeIn();

      };
      Controller.prototype.changeType = function(type){
          var options = this.view.modal.el.find('.J_option_area');
          var restricts = this.view.modal.el.find('.J_options_restrict');
          if(type=="blank"){
              options.hide();
              restricts.show();
          }else {
              options.show();
              restricts.hide();
          }
      };
      Controller.prototype.getData = function(){
          var el = this.view.modal.el;
          var type = el.find('.J_type').val();
          var options = [],content,desc,description;
          var restrict = el.find('.J_restrict').val();
          content = el.find('.J_content').val();
          desc = {content:content,options:options};
          if(type!=="blank"){
              el.find('.J_options input').each(function(){
                  desc.options.push($(this).val());
              });
          }else {
              desc.restrict = restrict;
          }
          description = JSON.stringify(desc);
          return {
              id:this.model.get('id'),
              type:type,
              desc:desc,
              description:description
          };
      };
      _.extend(Controller.prototype,Backbone.Events);

      return Controller;
  })();

  (function(){
      $(document).on('click','.J_question_edit',function(e){
          var target = $(e.target);
          var q = new Question({
              id:target.attr('data-id')
          },target);
          q.edit();
      });
  })();

  $(function() {
    var app = new AppRouter();
    Backbone.history.start();
  });

  window.Modal=Modal;
  window.Question = Question;

}).call(this);


(function(win){
    //Drag  Doc or Dir to another Dir
    var getUid =(function(){
        var id = 0;
        return function(){
            return id++;
        }
    })();

    var FolderDragger = function(item,id,type){
        this.item = item;
        this.id = id
        this.type = type;
        this.uid = getUid();
        this._init();
        FolderDragger.instances.push(this);
    };
    FolderDragger.prototype._init = function(){
        var self = this;
        //drag
        this.item.addEventListener('dragstart',function(e){
            console.log('drag');
            self.drag(e);
        },false);

        //drop
        if(this.type!=="doc"){
            this.item.addEventListener('dragover',function(e){
                e.preventDefault();
            },false);
            this.item.addEventListener('drop',function(e){
                e.preventDefault();
                self.drop(e);
            },false);
        }
    };
    FolderDragger.prototype.drag = function(e){
        e.dataTransfer.setData("uid",this.uid);
    };

    FolderDragger.prototype.drop = function(e){
        var from = FolderDragger.findInstanceByUid(e.dataTransfer.getData('uid'));
        if(!from || from.type==="trash"){
            return;
        }
        this.dropItIn(from.id,function(){
            from.item.remove();
        });
    };
    FolderDragger.prototype.dropItIn = function(id,cb){
        if(this.type==="trash"){
            this.remove(id,cb);
        }else {
            this.change(id,cb);
        }
    };
    FolderDragger.prototype.change = function(id,cb){
        //更改目录
        $.ajax({
            url:"/document/changedir",
            data:{
                id:id,
                parentId:this.id
            },
            type:"POST",
            dataType:"json",
            success:cb
        });
    };
    FolderDragger.prototype.remove = function(id,cb){
        //删除
        if(confirm("你确定要删除吗，删除后无法恢复!")){
            $.ajax({
                url:"/document/delete",
                data:{
                    id:id
                },
                type:"POST",
                dataType:"json",
                success:function(res){
                    if(res.code==200){
                        cb()
                    }else {
                        alert(res.data);
                    }

                }
            });
        }
    }

    FolderDragger.instances = [];

    FolderDragger.findInstanceByUid = function(id){
        var ins = this.instances.filter(function(instance){
            return instance.uid == id;
        });

        return ins?ins[0]:null;
    }

    win.FolderDragger = FolderDragger;
})(this);


(function(win){
    //新建文件夹

    var tpl = '<li data-type="dir" draggable="true" class="J_doc">\
                    <a href="javascript:;"><img src="/assets/images/folder.png"><span class="J_name"></span></a></li>';
    $(document).on('click','.J_folder_new',function(){
        var parentId = $(this).data('id');
        var folder = $(tpl).prependTo($('.document_list'))
        var name = folder.find('.J_name');
        name.editInPlace('init',{
            context:this,
            onChange:function(value){
                create($(this).data('id'),value);
            }
        })
        name.editInPlace('edit');
    });

    function create(parentId,name){
        $.ajax({
            url:"/document/create",
            data:{
                parentId:parentId,
                name:name
            },
            type:"POST",
            dataType:"json",
            success:function(res){
                if(res.code==200){
                    location.reload();
                }else {
                    alert(res.data);
                }
            }
        });
    }


})(this);

(function(){
    //分页

    var Page = function(outDom,documentId){
        //上下移
        $(outDom).on("click",".J_up",function(){
            var moveEl = $(this).parents('.J_move').eq(0);
            moveEl.insertBefore(moveEl.prev());
        });
        $(outDom).on('click',".J_down",function(){
            var moveEl = $(this).parents('.J_move').eq(0);
            moveEl.insertAfter(moveEl.next());
        });

        function editName(nameEl){
            nameEl.currentName = nameEl.text();
            nameEl.editInPlace('init',{
                context:this,
                onChange:function(value){
                    nameEl.editInPlace('close',value);
                    //改变select的值
                    $('.J_pages_select option').each(function(i,option){
                        if(option.text==nameEl.currentName){
                            option.text = value;
                        }
                    });
                    nameEl.currentName = value;
                }
            })
        }
        //修改名字
        $('.J_page_name').each(function(i,el){
            editName($(el));
        });

        //新建
        $(outDom).on('click','.J_new_page',function(){
            var pages = $('.J_page');
            var lastPage = pages.eq(pages.size()-1);
            var newPage = lastPage.clone().insertAfter(lastPage);
            newPage.find('.list-group').empty();
            var name = "新建分页"+(pages.size()+1);
            newPage.find('.J_page_name').text(name);
            newPage.find('.J_desc').val('');
            var pageNameEl = newPage.find('.J_page_name');

            editName(pageNameEl);
            pageNameEl.editInPlace('edit');

            //add an option
            $('.J_pages_select').append('<option>'+name+'</option>')
        });

        //选择分页
        $('.J_pages_select').change(function(){
            var value = $(this).val();
            var li  = $(this).parents(".list-group-item");
            $('.J_page_name').each(function(i,name){
                if($(name).text()===value){
                    $(name).parents('.J_page').find('.list-group').append(li);
                }
            });

        });

        //save
        $('.J_save').on('click',function(){
            var data = [];
            $('.J_page').each(function(){
                var pageItem = {};
                pageItem.name = $(this).find('.J_page_name').text();
                pageItem.desc = $(this).find('.J_desc').val();
                data.push(pageItem);
                pageItem.moduleList = [];

                $(this).find('.J_m').each(function(){
                    pageItem.moduleList.push({
                        id:$(this).data('id'),
                        type:$(this).data('type')
                    });
                });
            });
            data = {
                pageList:data
            }

            //ajax save
            $.ajax({
              url:"/page/save",
              data:{
                  id:documentId,
                  content:JSON.stringify(data)
              },
              type:"POST",
              dataType:"json",
              success:function(){
                  $('.J_suc').fadeIn();
                  setTimeout(function(){
                      $('.J_suc').fadeOut();
                  },3000);
              }

            });
        });
    }

    window.Page = Page;

})();

(function(){
    var currentId = null;
    window.Preview = function(){
        $(".J_preview").click(function(){
//        preview("/assets/javascripts/l-vim-script-1-pdf.pdf")
//        return;
           var id = currentId = $(this).data('id');
           new Modal('<div id="pdf_container"></div><canvas id="pdf-canvas"></canvas>','Preview');
           $.ajax({
                url:"/document/preview",
                data:{
                    id:id
                },
                type:"POST",
                dataType:"json",
                success:function(res){
                    if(id!=currentId){return}
                    //pdf
                    preview(res.data);
                },
                error:function(){
                }
           });
        });
    };

    function preview(url){
        var oc = document.getElementById('pdf-canvas');
        var container = document.getElementById('pdf_container');
        var scale = 1.2;
        PDFJS.getDocument(url).then(function(pdfDoc){
            function renderPage(num) {
                  // Using promise to fetch the page
                  pdfDoc.getPage(num).then(function(page) {
                    var viewport = page.getViewport(scale);
                    var canvas = oc.cloneNode()
                    canvas.style.display = "block";
                    container.appendChild(canvas);
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;

                    // Render PDF page into canvas context
                    var renderContext = {
                      canvasContext: canvas.getContext('2d'),
                      viewport: viewport
                    };
                    page.render(renderContext);
                    if(++num<pdfDoc.numPages){
                        renderPage(num);
                    }
                  });
            }

            renderPage(1);

        });
    }

})();

(function(){
    //模块依赖规则
    var getRules = function(documentId,id,onSuc){
        $.ajax({
            url:'/document/dependency',
            data:{
                documentId:documentId,
                moduleId:id
            },
            dataType:'json',
            type:"POST",
            success:function(res){
                res.data.questionList = res.data.questionList.filter(function(q){
                    q.description = {};
                    try{
                        q.description = JSON.parse(q.description);
                    }catch(e){}
                    return q.type === "choice" || q.type==="multichoice";
                });
                onSuc(res);
            },
            error:function(){
            }
        });
    };

    var template = '<div><ul class="d_rule_list">\
                    <%rules.forEach(function(rule,i){%>\
                        <li data-index="<%=i%>" data-ruleid="<%=rule.id%>">★ 如果 \
                        <%=rule.conditions[0].questionContent%> 选择了答案 <%=rule.conditions[0].questionOptions[rule.conditions[0].questionSelect]%>\
                        </li>\
                    <%});%>\
                    </ul>\
                    <div>当以上<b>任意条件</b>满足时，显示该模块</div>\
                    <div><a class="J_add">添加</a></div>\
    </div>';
    var conditionTemplate = '<div class="J_list"><%conditions.forEach(function(c,i){%>\
                                <%if(i>0){%> <p>并且</p><%}%>\
                                <div>当问题：<strong><%=c.questionContent%></strong>选择了答案<strong><%=c.questionOptions[c.questionSelect]%></strong><a class="J_del" href="javascript:;" data-id="<%=c.id%>">删除</a></div>\
                            <%});%></div><div><a class="J_add" href="javascript:;">添加</a></div>';
    var questionListTpl = '<div class="J_new"><%=prefix%>当问题：<select><%questionList.forEach(function(q){%>\
                            <option value="<%=q.id%>"><%=q.description.content%></option>\
    <%});%></select>选择了答案<a href="javascript:;" class="J_delnew">删除</a><div class="J_options"></div></div>';
    var optionTpl = '<%description.options.forEach(function(option,i){%>\
            <p><label><input type="radio" name="<%=id%>" <%if(i==0){%>checked<%}%> data-index="<%=i%>"/><%=option%></label></p>\
    <%});%>';
    var render = function(template,data){
        return _.template(template,data);
    };

    window.Rules = {
        edit:function(documentId,id){
            Rules.documentId = documentId;
            Rules.moduleId = id;
            getRules(documentId,id,function(data){
                var modal =Rules.modal= new Modal(render(template,data.data),'编辑依赖规则');
                Rules.data = data.data;
                Rules.events(modal.el);
                modal.on('save',Rules.save);
            });
        },
        events:function(el){
            el.find('.d_rule_list li').click(function(){
                var index = $(this).data('index');
                var ruleId = $(this).data('ruleid');
                var ruleData = Rules.data.rules[index];
                Rules.editRule(ruleId,ruleData);
            });
            el.find('.J_add').click(Rules.addRule);
        },
        addRule:function(){
            //编辑空的rule
            Rules.editRule("",{
                conditions:[],
                ruleId:""
            });
        },
        editRule:function(ruleId,ruleData){
            var modal = new Modal(render(conditionTemplate, ruleData),'编辑条件');
            modal.el.find('.J_add').on('click',function(){
                var list = modal.el.find('.J_list')
                list.append(Rules.createCondition(!!list.children().length));
            });
            modal.el.find('.J_del').on('click',function(){
                var btn = $(this);
                if(confirm('确定要删除这条规则吗?')){
                    Rules.ajaxDeleteCondition(btn.data('id'),function(){
                        btn.parent().remove();
                    });
                }
            });
            modal.on('save',function(){
                modal.el.find('.J_new').each(function(i,newCondition){
                    var id = $(newCondition).find('select').val();
                    var option = $(newCondition).find('.J_options input:checked').data('index');
                    Rules.ajaxAddCondition(ruleId,id,option);
                });
                modal.hide();
                Rules.modal.hide();

                alert('保存成功');
            });
        },
        ajaxAddCondition:function(ruleId,questionId,optionId){
            $.ajax({
                url:"/document/dependency/condition/add",
                data:{
                    moduleId:Rules.moduleId,
                    ruleId:ruleId,
                    questionId:questionId,
                    optionId:optionId
                },
                type:"POST"
            });
        },
        ajaxDeleteCondition:function(id,cb){
            $.ajax({
                url:"/document/dependency/condition/delete",
                data:{
                    id:id
                },
                type:"POST",
                success:cb
            });
        },
        createCondition:function(hasAnd){
            Rules.data.prefix = hasAnd?'<p>并且</p>':"";
            var html = _.template(questionListTpl,Rules.data);
            var el = $(html);
            el.find('select').change(function(){
                var index = this.selectedIndex;
                el.find('.J_options').html(_.template(optionTpl,Rules.data.questionList[index]));
                el.find('.J_options input').attr('name',Math.random());
            });
            el.find('.J_options').html(_.template(optionTpl,Rules.data.questionList[0]));
            el.find('.J_options input').attr('name',Math.random());

            el.find('.J_delnew').click(function(){
                el.remove();
            });
            return el;
        }
    };
})();

(function(){
    //编辑文档级问题

    var listTpl = '<p><a href="javascript:;" class="J_add">添加问题</a></p><ul><%data.forEach(function(item){%>\
                <li data-id="<%=item.questionId%>"><%=item.question.description.content%></li>\
    <%});%></ul>';
    var ModuleQuestion = function(moduleId){
        this.moduleId = moduleId;
    };

    ModuleQuestion.prototype.edit = function(){
        var self = this;
        $.ajax({
            url:"/documentquestions/"+this.moduleId,
            type:"GET",
            dataType:"json",
            success:function(res){
                self.showList(res);
            }
        });
    };
    ModuleQuestion.prototype.render = function(data){
        return _.template(listTpl,data);
    };
    ModuleQuestion.prototype.showList = function(data){
        var self = this;
        data.data.forEach(function(item){
            try{
                item.question.description = JSON.parse(item.question.description);
            }catch(e){
                item.question.description = {};
            }
        });
        var modal = this.modal = new Modal(self.render(data),'所有问题');
        modal.el.find('.J_add').click(function(){
            self.add();
        });
        modal.el.on('click','li',function(e){
            self.editQuestion($(this));
        });
        modal.on('save',function(){
            modal.hide();

        });
    };
    ModuleQuestion.prototype.add = function(){
        var self = this;
        var q = new Question({
            id:"new"
        });
        q.edit();
        q.on('save',function(model){
            var description = {};
            try{
                description = JSON.parse(model.get('description'));
            }catch(e){}
            //保存成功,显示这个问题
            self.modal.el.find('ul').append('<li data-id="'+model.get('id')+'">'+description.content+'</li>');
            self.save(model.get('id'));
        });
    };

    ModuleQuestion.prototype.editQuestion = function(el){
        //编辑问题
        var q = new Question({
            id:el.data('id')
        });
        q.edit();
        q.on('save',function(model){
            var description = JSON.parse(model.get('description'));
            el.html(description.content);
        });
    };

    ModuleQuestion.prototype.save = function(questionId){
        //保存问题关系
        $.ajax({
            url:"modulequestionsave",
            data:{
                moduleId:this.moduleId,
                questionId:questionId
            },
            type:"POST",
            success:function(){
            }
        });
    };


    window.ModuleQuestion = ModuleQuestion;
})();
