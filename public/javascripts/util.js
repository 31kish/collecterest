$(function() {

 $('#share_url').trigger('autoresize');

//LeftColum Scroll
 var colum_offset = $('.left_colum').offset().top;
 $(window).on('scroll load',function(){
 	var now_offset = $(window).scrollTop();
 	if( now_offset >= colum_offset) {
 		$('.left_colum').css('position','fixed');
 	}
 	else {
 		$('.left_colum').css('position','inherit');
 	}
 });

 var error_dialog = $('.error').text();
 Materialize.toast(error_dialog,2000);

$(document).on('click', '.view_link', function() {
	var id = $(this).data('id');
	$.ajax({
            type: "POST",
            url: "/view",
            data: {
            	articleID : id
            },
            success: function(j_data){
                // 処理を記述
 				$('.count.view').filter(function() {
 					return $(this).data('id') == id
 				}).text(j_data);
            }
        });

});

});