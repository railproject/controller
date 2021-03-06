/**
 * 计划信息展示页面
 * 
 * @author denglj 1.接收计划设置参数并转发后台 2.显示计划信息
 */
var PlanViewPage = function(){

	var _self = this;
	var LocString = window.document.location.href;
	this.getParameter = function(str) {
	    var rs = new RegExp("(^|)" + str + "=([^&]*)(&|$)", "gi").exec(LocString), tmp;
	    if (tmp = rs) {
	    	return decodeURI(decodeURI(tmp[2]));
	    }
	    return "";
	};
	
	
	/**
	 * socket连接
	 */
	_self.connect = function() {
	     stompClient.connect({}, function(frame) {
	    	 
	    	 //监听xx日计划开始事件
			 stompClient.subscribe("/railwayplan/import.plan.day.detail", function(message) {
				 
				 console.log('~~~~~~~~~~~ 收到开始消息:'+JSON.parse(message.body));
//			      self.portfolio().processQuote(JSON.parse(message.body));
				 var obj = JSON.parse(message.body);
      
			     
			 });
			 
			 //监听xx日计划处理中事件
			 stompClient.subscribe("/railwayplan/import.plan.day.begin", function(message) {

				 console.log('~~~~~~~~~~~ 收到每天开始消息:'+JSON.parse(message.body));
				 var obj = JSON.parse(message.body);
				 _self.startPlanDay(obj);
//				 $("#"+obj.rundate+"JlSum").text(obj.crosscount);
//				 $("#"+obj.rundate+"TrainSum").text(obj.traincount);
			     
			 });
			 
			 //监听xx日计划结束事件
			 stompClient.subscribe("/railwayplan/import.plan.day.end", function(message) {
				 console.log('~~~~~~~~~~~ 收到每天结束消息:'+JSON.parse(message.body));
				 var obj = JSON.parse(message.body);
				 _self.finishPlanDay(obj);
//				 $("#"+obj.rundate+"JlSum").text(obj.crosscount);
//				 $("#"+obj.rundate+"TrainSum").text(obj.traincount);
//				 $("#"+obj.rundate+"DisplayStatus").text("完成");
//				 $("#"+obj.rundate+"Status").attr("class", "label label-danger");
			     
			 });
			 
			 //监听xx计划结束事件
			 stompClient.subscribe("/railwayplan/import.plan.end", function(message) {
				 console.log('~~~~~~~~~~~ 收到计划全部结束消息:'+JSON.parse(message.body));
				 _self.finishPlan();
//				 $("#plan_view_label_planCurrentStatus").text(_param_days+"天开行计划已全部完成");//界面顶端显示
			     
			 });
			 
			 
			//2.调用执行开行计划
			 _self.importTrainPlan();
	    }, function(error) {
//	    	console.log("STOMP protocol error " + error);
	    });
	};
	_self.logout = function() {
		stompClient.disconnect();
		window.location.href = "../logout.html";
	};

	
	
	this.initPage = function() {
		_plan_view_table_1.find("tr:gt(0)").remove();//清除计划列表所有数据
		
		// 获取页面传值
		_param_schemeId = _self.getParameter("schemeVal");
		var schemeText = _self.getParameter("schemeText");
		_param_days = _self.getParameter("days");
		_param_startDate = _self.getParameter("startDate");
		
		_plan_view_label_days.text(_param_days);
		_plan_view_label_schemeText.text(schemeText);
		_plan_view_label_startDate.text(_param_startDate);
		
		_self.renderJlmxHightChart();
		
		//初始化socket连接
		_self.connect();
		
		
	};
	
	
	//调用执行开行计划
	_self.importTrainPlan = function() {
		$.ajax({
			url : "../../plan/importTrainPlan",
			cache : false,
			type : "POST",
			dataType : "json",
			contentType : "application/json",
			data :JSON.stringify({
				startDate : _param_startDate,
				dayCount:_param_days,
				schemeId:_param_schemeId
			}),
			success : function(result) {
				if (result != null && result != "undefind" && result.code == "0") {
					_hightChart_jlmx.setTitle({
			            text: _param_startDate+"~"+result.data.enddate+'开行计划概况'
			        });
				} else {
					alert("接口调用返回错误，code="+result.code+"   message:"+result.message);
				}

			},
			error : function() {
				alert("接口调用失败");
//				showErrorDialog("请求发送失败");
//				showErrorDialog(portal.common.dialogmsg.REQUESTFAIL);
			}
		});
	};
	
	
	
	this.renderJlmxHightChart  = function() {
		_hightChart_jlmx = new Highcharts.Chart({//.highcharts({
	        chart: {
	        	renderTo: 'hightChart_lcmx',
	            type: 'column'
	        },
	        title: {
	            text: '开行计划概况'
	        },
	        subtitle: {
	            text: '车次统计'
	        },
	        scrollbar: { enabled: true },
	        xAxis: {
	            categories: _hightChart_jlmx_xAxisArray
	        },
	        yAxis: {
	            min: 0,
	            title: {
	                text: '单位 (车次)'
	            }
	        },
	        tooltip: {
	            shared: true,
	            useHTML: true,
	            formatter : function() {
	            	var retStr = '<span style="font-size:10px">日期：'+_self.nextDayMap(this.x)+'</span><table>';
	            	for(var i =0;i<this.points.length;i++) {
	            		retStr =retStr+'<tr><td style="color:'+this.points[i].series.color+';padding:0">'+this.points[i].series.name+': </td>' +
	            		'<td style="padding:0"><b>'+this.points[i].y+'车次</b></td></tr>';
	            	}
	            	retStr =retStr+'</table>';
	            	return retStr;
				}
	            
	        },
	        plotOptions: {
	            column: {
	                pointPadding: 0.1,
	                borderWidth: 0
	            }
	        },
	        series: [
	                /* {
	            name: '交路',
	            data: _hightChart_jlmx_jldata

	        },*/ {
	            name: '车次',
	            data: _hightChart_jlmx_lcdata

	        }]
	    });
	};
	
	
	
	
	
	_self.nextDayMap =function(_dayIndex){
		_dayIndex = parseInt(_dayIndex)-1;
		return _hightChartDateMap[_dayIndex];
	};
	
	
	
	
	/**
	 * xx日计划开始处理方法
	 * @param _dayIndex 第几天序号 如：10
	 * @author 邓柳江
	 */
	_self.startPlanDay = function(planObj) {
		var _dayIndex = planObj.dayindex;
//		console.log("~~~~startPlanDay _dayIndex:"+_dayIndex);
		//1.生成图表新节点
		//填充x、y轴数组数据
		_hightChart_jlmx_xAxisArray.push(_dayIndex);
		_hightChart_jlmx_jldata.push(planObj.crosscount);//self.jlMathRand());
		_hightChart_jlmx_lcdata.push(planObj.traincount);//self.lcMathRand());
		//更新图表数据
		_hightChart_jlmx.xAxis[0].setCategories(_hightChart_jlmx_xAxisArray);
//		_hightChart_jlmx.series[0].setData(_hightChart_jlmx_jldata);
		_hightChart_jlmx.series[0].setData(_hightChart_jlmx_lcdata);
		_hightChart_jlmx.redraw();//刷新图表
		$("#plan_view_label_refreshtime1").text(planObj.refreshtime);
		$("#plan_view_label_refreshtime2").text(planObj.refreshtime);
		
		
		//2.列表明细增加一条记录
//		var obj = new Object();
//		if(_dayIndex > 1) {
//			obj.rundate = self.nextDay(_dayIndex);
//		} else {
//			obj.rundate = _param_startDate;
//		}
		_hightChartDateMap.push(planObj.rundate);
		var tr;
//		var num = _plan_view_table_1.find("tr:gt(0)").length+1;
//		var num = $("#plan_view_table_tbody").find("tr:gt(0)").length+1;
		tr = "<tr id='plan_view_grid_tr"+_dayIndex+"' onclick=showPalnDayDetail('"+planObj.rundate+"')>";
//		tr.append("<td style='display: none'>"+obj.rundate+"</td>");
//		tr.append("<td>"+num+"</td>");
//		tr.append("<td><a href='#'>"+planObj.rundate+"</a></td>");
//		tr.append("<td id='plan_view_grid_JlSum"+_dayIndex+"'></td>");
//		tr.append("<td id='plan_view_grid_TrainSum"+_dayIndex+"'></td>");
//		tr.append("<td id='plan_view_grid_DisplayStatus"+_dayIndex+"'>正在处理中</td>");
//		tr.append("<td><span id='plan_view_grid_Status"+_dayIndex+"' class='label label-success'>处理中</span></td>");
//		tr.append("</tr>");
		tr +="<td>"+_dayIndex+"</td>";
		tr +="<td><a href='#'>"+planObj.rundate+"</a></td>";
		tr+="<td id='plan_view_grid_JlSum"+_dayIndex+"'></td>";
		tr+="<td id='plan_view_grid_TrainSum"+_dayIndex+"'></td>";
		tr+="<td id='plan_view_grid_DisplayStatus"+_dayIndex+"'>正在处理中</td>";
		tr+="<td><span id='plan_view_grid_Status"+_dayIndex+"' class='label label-success'>处理中</span></td>";
		tr+="</tr>";
//		_plan_view_table_1.append(tr);
		if (_dayIndex == 1) {
			$("#plan_view_table_tbody").append(tr);
		} else {
			var num = _dayIndex-1;
			$("#plan_view_grid_tr"+num).before(tr);
		}
		
//		if (_dayIndex >= _param_days) {
//			//设置列表排序
//			oTable = $('#plan_view_table_1').dataTable({
//				"sPaginationType": "full_numbers"
//			});
//		}
		
		
		//3.更新界面顶端进度说明
		var _finishRecord = _dayIndex-1;
		var _remainRecord = _param_days-_dayIndex;
		var _currentProcessStatus = "正在生成第（"+_dayIndex+"）天开行计划，已完成（"+_finishRecord+"）天开行计划，还剩（"+_remainRecord+"）天数据未生成";
		$("#plan_view_label_planCurrentStatus").text(_currentProcessStatus);//界面顶端显示
		
//		dayIndex = _dayIndex+1;
	};
	
	
	
	/**
	 * 车次列表行点击事件
	 * 显示xx日车次详细信息
	 * @author 邓柳江
	 */
	self.showPalnDayDetail = function (rundate) {
		_plan_view_div_palnDayDetail.show(); //显示车次详情div
		_plan_view_div_palnDayDetail_title.text(rundate);
		
		_self.queryTrainDetail();//查询该日期内车次详细信息
	};
	
	
	/**
	 * xx日计划结束处理方法
	 * @param _dayIndex 第几天序号 如：10
	 * @author 邓柳江
	 */
	_self.finishPlanDay = function (dayPlanObj) {
		var _dayIndex = dayPlanObj.dayindex;

		$("#plan_view_label_refreshtime1").text(dayPlanObj.refreshtime);
		$("#plan_view_label_refreshtime2").text(dayPlanObj.refreshtime);
		
		//1.更新图表数据
		_self.updateHightChartData(_dayIndex, dayPlanObj.crosscount, dayPlanObj.traincount);
//		_hightChart_jlmx.series[0].setData(_hightChart_jlmx_jldata);
		_hightChart_jlmx.series[0].setData(_hightChart_jlmx_lcdata);
		_hightChart_jlmx.redraw();//刷新图表
		
		
		
		//2.更新gird列表状态为“完成”
		$("#plan_view_grid_JlSum"+_dayIndex).text(dayPlanObj.crosscount);
		$("#plan_view_grid_TrainSum"+_dayIndex).text(dayPlanObj.traincount);
		$("#plan_view_grid_DisplayStatus"+_dayIndex).text("完成");
		$("#plan_view_grid_Status"+_dayIndex).attr("class", "label label-danger");
		$("#plan_view_grid_Status"+_dayIndex).text("完成");
		
		//设置列表排序
//		oTable = $('#plan_view_table_1').dataTable({
//			"sPaginationType": "full_numbers"
//		});
	};
	
	
	_self.updateHightChartData = function(_dayIndex, _crosscount, _traincount) {
		var tempJlArray = [];
		var tempLcArray = [];
		for ( var m = 0; m < _hightChart_jlmx_xAxisArray.length; m++) {
			if (m == (_dayIndex-1)) {
				tempJlArray.push(_crosscount);
				tempLcArray.push(_traincount);
			} else {
				tempJlArray.push(_hightChart_jlmx_jldata[m]);
				tempLcArray.push(_hightChart_jlmx_lcdata[m]);
			}
			
		}
		_hightChart_jlmx_jldata = tempJlArray;
		_hightChart_jlmx_lcdata = tempLcArray;
	};
	
	
	
	/**
	 * xx计划全部结束处理方法
	 */
	_self.finishPlan = function () {
		 $("#plan_view_label_planCurrentStatus").text(_param_days+"天开行计划已全部完成");//界面顶端显示
	};
	
	
	
	
	
	/**
	 * 查询指定日期车次详细信息
	 * 
	 * @author 邓柳江
	 */
	_self.queryTrainDetail = function() {
		_plan_view_div_palnDayDetail_trainDetail.find("tr:gt(0)").remove();//清除车次详细信息列表所有数据
		$.ajax({
			url : "../../plan/getTrainShortInfo",
			cache : false,
			type : "POST",
			dataType : "json",
			contentType : "application/json",
			data :JSON.stringify({
				runDate : _plan_view_div_palnDayDetail_title.text(),//开行日期
				trainNbr : _plan_view_div_palnDayDetail_trainNum.val(),//车次号
				count : 35//pagesize
			}),
			success : function(result) {
				if (result != null && result != "undefind" && result.code == "0") {
					if (result.data !=null) {
						$.each(result.data,function(n,trainObj){
							tr = $('<tr/>');
							tr.append("<td>"+trainObj.trainNbr+"</td>");
							tr.append("<td>"+trainObj.startStn+"</td>");
							tr.append("<td>"+trainObj.endStn+"</td>");
							_plan_view_div_palnDayDetail_trainDetail.append(tr);
						});
					}
				} else {
					alert("接口调用返回错误，code="+result.code+"   message:"+result.message);
				}

			},
			error : function() {
				alert("接口调用失败");
//				showErrorDialog("请求发送失败");
//				showErrorDialog(portal.common.dialogmsg.REQUESTFAIL);
			}
		});
	};
	
	
	
	
	
};

var _PlanViewPage = null;
var _plan_view_label_planErrorRecord = $("#plan_view_label_planErrorRecord");// 错误数总计
var _hightChart_jlmx = null;//$("#hightChart_jlmx");// 交路信息统计图表
var _hightChart_lcmx = null;//$("#hightChart_lcmx");// 列车信息统计图表
var _plan_view_label_schemeText = $("#plan_view_label_schemeText");// 方案
var _plan_view_label_days = $("#plan_view_label_days");// 天数
var _plan_view_label_startDate = $("#plan_view_label_startDate");// 开始日期
var _plan_view_table_1 = $("#plan_view_table_1");// 开行计划信息列表
var _plan_view_div_palnDayDetail = $("#plan_view_div_palnDayDetail");// 车次详情div
var _plan_view_div_palnDayDetail_title = $("#plan_view_div_palnDayDetail_title");// 车次详情div title
var _plan_view_div_palnDayDetail_trainNum = $("#plan_view_div_palnDayDetail_trainNum");// 车次详情div 车次号input
var _plan_view_div_palnDayDetail_btnQuery = $("#plan_view_div_palnDayDetail_btnQuery");// 车次详情div 车次查询按钮
var _plan_view_div_palnDayDetail_trainDetail = $("#plan_view_div_palnDayDetail_trainDetail");// 车次详情div 车次详情列表


//来源页面传递参数
var _param_schemeId = null;
var _param_startDate = null;	
var _param_days = null;

var _hightChartDateMap = [];//将图表横坐标dayIndex值与日期对应保存 ,用于图表tooltip事件
var _hightChart_jlmx_xAxisArray = [];//横坐标值
var _hightChart_jlmx_jldata = [];	//交路data
var _hightChart_jlmx_lcdata = [];	//列车data


var socket = null;
var stompClient = null;
$(function(){
	_PlanViewPage = new PlanViewPage();
	_plan_view_div_palnDayDetail.hide(); //隐藏车次详情div
	_plan_view_label_planErrorRecord.text("0");// 错误数总计0条
	
	//车次详情div 车次查询按钮增加事件
	_plan_view_div_palnDayDetail_btnQuery.click(function(){
		_PlanViewPage.queryTrainDetail();
	});
	//车次详情div 车次号input 增加回车事件
	_plan_view_div_palnDayDetail_trainNum.keypress(function(event){
	    var keycode = (event.keyCode ? event.keyCode : event.which);
	    if(keycode == '13'){
	    	_PlanViewPage.queryTrainDetail();
	    }
	});
	
	
	//创建websocket连接
	socket = new SockJS('/railwayplan-1/portfolio');
    stompClient = Stomp.over(socket);
	_PlanViewPage.initPage();

	
});



