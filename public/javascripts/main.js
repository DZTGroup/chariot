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


  AppRouter = (function(_super) {
    __extends(AppRouter, _super);

    function AppRouter() {
      return AppRouter.__super__.constructor.apply(this, arguments);
    }

    AppRouter.prototype.routes = {
      "/documents": "documents",
      "/fileUpload":"fileUpload",
      "/document/:id": "document"
    };

    AppRouter.prototype.documents = function() {
      return $("#main").load("/documents");
    };

    AppRouter.prototype.fileUpload = function() {
        return $("#main").load("/fileUpload");
      };

    AppRouter.prototype.document = function(id) {
      return $("#main").load("/document/"+id);
    };

    return AppRouter;

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
      M.hide = function(){
          this.el.$data('modal').hide();
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
      var TPL = '<div class="question_editor"><table><tbody>'+
          '<tr><td>问题类型:</td><td><select class="J_type"><%_.each(types,function(value,key){%><option value="<%=key%>"><%=value%></option><%});%></select></td></tr>'+
          '<tr>'+
            '<td>问题:</td><td><textarea placeholder="问题描述"></textarea></td>'+
          '</tr>'+
          '</tbody></table></div>';
      var Model = Backbone.Model.extend({
          defaults:{
              types:TYPES
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

      var Controller = function(attrs){
          this.model = new Model(attrs);
          this.view = new View({
              model:this.model
          });
      };
      Controller.prototype.edit = function(){
          this.view.render();
          this.view.modal.bind('save',function(){
          });
      };
      Controller.prototype.save = function(){
          this.model.save();
      };
      return Controller;
  })();

  (function(){
      $(document).on('click','.J_question_edit',function(){
          var q = new Question();
          q.edit();
      });
  })();

  $(function() {
    var app, drawer;
    app = new AppRouter();
    return Backbone.history.start({
      pushHistory: true
    });
  });


}).call(this);
