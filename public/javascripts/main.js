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
      var router = Router.extend({
          routes:{
              "documents": "documents",
              "fileUpload":"fileUpload",
              "document/:id": "document"
          },
          documents:function(){
              return $("#main").load("/documents");
          },
          fileUpload:function(){
              return $("#main").load("/fileUpload");
          },
          document:function(id){
              return $("#main").load("/document/"+id);
          }
      });

    return router;

  })(Backbone.Router);


  //Modal box
  var Modal = (function(){
      var tpl = '<div class="modal hide fade modal-overflow in" tabindex="-1" data-width="760"  aria-hidden="false">'+
                        '<div class="modal-header">'+
                            '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'+
                            '<h3>Responsive</h3>'+
                        '</div>'+
                        '<div class="modal-body"></div>'+
                        '<div class="modal-footer">'+
                            '<button type="button" data-dismiss="modal" class="btn">关闭</button>'+
                            '<button type="button" class="btn btn-primary J_save">保存</button>'+
                        '</div>'+
                    '</div>';
      var M = function(contentHtml){
          var self = this;
          var el = this.el = $(tpl);
          el.find('.modal-body').append(contentHtml)
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
          mutichoice:"多选"
      };
      var getIndex = function(i){
          return LETTERS.charAt(i);
      };
      var TPL = '<div class="question_editor"><table><tbody>'+
          '<tr><td>问题类型:</td><td><select class="J_type"><%_.each(types,function(value,key){%><option value="<%=key%>"><%=value%></option><%});%></select></td></tr>'+
          '<tr>'+
            '<td>问题:</td><td><textarea placeholder="问题描述" class="J_content"><%=desc.content%></textarea></td>'+
          '</tr></tbody></table>'+
          '<div class="J_option_area options_area"><p><button type="button" class="btn btn-primary J_option_add">+选项</button></p>'+
          '<ul class="J_options options_list">'+
          '<%if(desc.options){desc.options.forEach(function(item,i){%>'+
          '<li><%=(i+1)%>.<input type="text" value="<%=item%>"><a onclick="$(this).parent().remove();" href="javascript:;">x</a></li>'+
          '<%})}%>'+
          '</ul></div></div>';
      var DISPLAY_TPL = '<dl class="questions"><dt><%=desc.content%></dt>'+
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
                  var desc = JSON.parse(data.description);
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
              this.modal = new Modal(html);
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
              btn.parent().next().remove()
              btn.parent().after(self.displayView.render());
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
              success:function(){
                  self.view.modal.hide();
                  self.model.set(data);
              }
          });
      };
      Controller.prototype.addOption = function(){
          var options = this.view.modal.el.find('.J_options');
          $('<li>'+(options.find('li').length+1)+'.<input type="text" ><a onclick="$(this).parent().remove();" href="javascript:;">x</a></li>')
          .appendTo(options).hide().fadeIn();

      };
      Controller.prototype.changeType = function(type){
          var options = this.view.modal.el.find('.J_option_area');
          if(type=="blank"){
              options.hide();
          }else {
              options.show();
          }
      };
      Controller.prototype.getData = function(){
          var el = this.view.modal.el;
          var type = el.find('.J_type').val();
          var options = [],content,desc,description;
          content = el.find('.J_content').val();
          if(type!=="blank"){
              el.find('.J_options input').each(function(){
                  options.push($(this).val());
              });
          }
          desc = {content:content,options:options};
          description = JSON.stringify(desc);
          return {
              id:this.model.get('id'),
              type:type,
              desc:desc,
              description:description
          };
      };
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


}).call(this);
