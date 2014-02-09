(function(){
    var BASE_COOKIE_KEY = "interview_";
    var documentId = null;
    var answers = {};
    window.Interview = function(did){
        documentId = did;

        var answers = $.cookie(BASE_COOKIE_KEY+documentId);
        if(answers){
            try{
                answers = JSON.parse(answers);
            }catch(e){
                answers = {};
            }
        }
    };

    var getPageAnser = function(){
        $('.J_question').each(function(i,question){
            var q = $(question);
            var name = q.attr('name');
            switch(question.type){
                case "text":
                    answers[name] = q.val();
                    break;
                case "radio":
                    if(question.checked){
                        answers[name] = q.val();
                    }
                    break;
                case "checkbox":
                    if(question.checked){
                        if(name in answers){
                            answers[name].push([q.val()]);
                        }else {
                            answers[name] = [q.val()];
                        }
                    }
                    break;
            }
        });

        $.cookie(BASE_COOKIE_KEY+documentId,JSON.stringify(answers),{
            expires:1,
            path:'/'
        });
    };

    //go on button

    $(".J_goon").click(getPageAnser);
    $(".J_submit").click(function(){
        getPageAnser();
        $('.J_answer').val(JSON.stringify(answers));
    });
    $('.J_create').click(function(){
        $.removeCookie(BASE_COOKIE_KEY+$(this).attr('data-value'));

    });
})();
