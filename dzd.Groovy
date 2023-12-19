ActionAttribute attribute = ActionAttribute.build {
   triggerApprovalFlow = false
   triggerWorkflow = false
   skipFunctionAction = true
   specifyCreatedBy = true
   specifyTime = true
   duplicateSearch = false
}
//李应霞ID 
List ownerList = ["1476"] as List
List ids =  context.objectIds as List
def(boolean error,List dataList,String errorMessage) = Fx.object.findByIds("AccountObj",ids)

dataList.each{ item ->
  Map map = item as Map
  //处理业务逻辑
  List 逾期未付列表 = []
  String 客户名称 = map["_id"]
  String 客户名字 = map["name"]
  List 客户负责人 = map["owner"] as List
  // BigDecimal 客户总欠款 = map["field_ROx2I__c"] as BigDecimal
  Date toDay = Date.now() as Date
  Date 对账月份 = Date.now() as Date
  对账月份 = 对账月份 - 1.months
  String 对账月 = 对账月份.month as String
  if(对账月.length() == 1){
    对账月 = "0" + 对账月
  }
  String 对账年份 = 对账月份.year as String
  String 对账年月 = 对账年份 + 对账月

  
  String 客户编号 = map["field_9k2mn__c"] 

  List 编码拆分 = 客户编号.split("\\.") as List
  String 拆分一 = 编码拆分[1] as String
  String 拆分二 = 编码拆分[2] as String
  
  String 对账单编码后缀 = 拆分一 + 拆分二

  
  String 对账单编码 = 对账年月 + "-" + 对账单编码后缀
  BigDecimal 本期待付金额 = 0
  List 对账明细列表 = []
  List 发货明细列表 = []
  List 退货明细列表 = []
  
  Date 月份开始日期 = 对账月份.withDay(1)
  Date 月份结束日期 = 月份开始日期 + 1.months
  // 月份结束日期 = 月份结束日期 - 1.days
  
  List 当期回款计划汇总 = []
  
  
  Range range1 = Ranges.of(0,100)

  range1.each{
      it ->
      
      Integer 翻页条数 = it *100 as Integer
    APIResult ret = Fx.object.find("PaymentPlanObj", //查询回款计划对象
               FQLAttribute.builder()
                  .columns(["_id", "name","plan_payment_time","order_id","field_O28Jb__c",
                  "field_dUTxk__c","field_w4Q8V__c","field_TOa0g__c","field_n6iq1__c"]) //返回的数据id
                  .queryTemplate( QueryTemplate.AND(["account_id": 客户名称],["plan_payment_time" : Operator.GTE(月份开始日期)],
                  ["plan_payment_time" : Operator.LT(月份结束日期)],["field_n6iq1__c" : Operator.NE("q7xWQqzwJ")]))
                  .limit(100)
                  .skip(翻页条数)
                  .build(),
               SelectAttribute.builder()
                  .build());
    if(ret.isError()){
        log.info(ret.message());
    }
  
  
  List dataList2 = ret.data["dataList"] as List
  Integer dataList2Size = dataList2.size() as Integer
    if(dataList2Size > 0){
        dataList2.each{ item4 ->
        Map 本期应收明细 = item4 as Map
        if(!当期回款计划汇总.contains(本期应收明细)){
        当期回款计划汇总.add(本期应收明细)
        }
      }
    }
   
  
  } 
  
  
 
  当期回款计划汇总.each{item2 ->
      Map hkjhMap = item2 as Map
      String 回款计划 = hkjhMap["_id"]
      Date 计划回款日期 = hkjhMap["plan_payment_time"]
      String 销售订单 = hkjhMap["order_id"]
      String 合同编号 = hkjhMap["field_O28Jb__c"]
      Date 订单日期 = hkjhMap["field_dUTxk__c"] as Date
      BigDecimal 订单金额 = hkjhMap["field_w4Q8V__c"] as BigDecimal
      BigDecimal 本期待回款金额 = hkjhMap["field_TOa0g__c"] as BigDecimal
      BigDecimal 本期累计待回款金额 = 0
      String 回款计划类型 = hkjhMap["field_n6iq1__c"]
      if(回款计划类型 == "nRqTUyYYU") 回款计划类型 = "Emi7sv3Ya"
      // APIResult ret3 = Fx.object.findById("SalesOrderObj", //查询订单对象
      //       销售订单,
      //       FQLAttribute.builder()
      //           .columns(["_id","name","receivable_amount","payment_amount","field_vj4oM__c"]) //返回的数据id
      //           .build(),
      //       SelectAttribute.builder()
      //           .build());
      // if(ret3.isError()){
      //     log.info(ret3.message());
      // }
      
      // Map salesOrderMap = ret3.data as Map 
      
      // BigDecimal 订单待回款金额 = salesOrderMap["receivable_amount"] as BigDecimal
      // if(!订单待回款金额){
      //   订单待回款金额 = 0
      // }
      // BigDecimal 订单已回款金额 = salesOrderMap["payment_amount"] as BigDecimal
      // if(!订单已回款金额){
      //   订单已回款金额 = 0
      // }
      
      Map dzmxMap = [
        "field_sv3xo__c" : 回款计划,
        "field_Xya1L__c" : 计划回款日期,
        "field_U01EY__c" : 销售订单,
        "field_gw1Ef__c" : 合同编号,
        "field_9W91g__c" : 订单日期,
        "field_MGh87__c" : 订单金额,
        // "field_qpf12__c" : 订单待回款金额,
        // "field_vuwHu__c" : 订单已回款金额,
        "field_5c634__c" : 本期待回款金额,
        "field_m8ss5__c" : 回款计划类型,
        "owner" : 客户负责人,
        "record_type" : "default__c"
        ]
      对账明细列表.add(dzmxMap)
    }
    
    List 超期回款计划明细 = []
    
    Range range2 = Ranges.of(0,5)
      
    range2.each{
      it ->
        Integer 翻页条数 = it *100 as Integer
    
    APIResult ret7 = Fx.object.find("PaymentPlanObj", //查询回款计划对象
              FQLAttribute.builder()
                  .columns(["_id", "name","plan_payment_time","order_id","field_O28Jb__c",
                  "field_dUTxk__c","field_w4Q8V__c","field_TOa0g__c","field_n6iq1__c"]) //返回的数据id
                  .queryTemplate( QueryTemplate.AND(["account_id": 客户名称],["plan_payment_time" : Operator.LT(月份开始日期)],["field_n6iq1__c" : Operator.NE("q7xWQqzwJ")],["field_TOa0g__c":Operator.GT(0)]
                  ))
                  .orderBy(["plan_payment_time":1])
                  .limit(100)
                  .skip(翻页条数)
                  .build(),
              SelectAttribute.builder()
                  .build());
    if(ret7.isError()){
        log.info(ret7.message());
    }
    
    List dataList7 = ret7.data["dataList"] as List
    Integer dataList7Size = dataList7.size() as Integer

    if(dataList7Size > 0){
        dataList7.each{ item4 ->
        Map 逾期应收明细 = item4 as Map
        if(!超期回款计划明细.contains(逾期应收明细)){
        超期回款计划明细.add(逾期应收明细)
        }
      }
    }
    }
    
    
  超期回款计划明细.each{item7 ->
      Map hkjhMap = item7 as Map
      String 回款计划 = hkjhMap["_id"]
      Date 计划回款日期 = hkjhMap["plan_payment_time"]
      String 销售订单 = hkjhMap["order_id"]
      String 合同编号 = hkjhMap["field_O28Jb__c"]
      Date 订单日期 = hkjhMap["field_dUTxk__c"] as Date
      BigDecimal 订单金额 = hkjhMap["field_w4Q8V__c"] as BigDecimal
      BigDecimal 本期待回款金额 = hkjhMap["field_TOa0g__c"] as BigDecimal
      BigDecimal 本期累计待回款金额 = 0
      String 回款计划类型 = hkjhMap["field_n6iq1__c"]
      if(回款计划类型 == "nRqTUyYYU") 回款计划类型 = "Emi7sv3Ya"
      // APIResult ret3 = Fx.object.findById("SalesOrderObj", //查询订单对象
      //       销售订单,
      //       FQLAttribute.builder()
      //           .columns(["_id","name","receivable_amount","payment_amount","field_vj4oM__c"]) //返回的数据id
      //           .build(),
      //       SelectAttribute.builder()
      //           .build());
      // if(ret3.isError()){
      //     log.info(ret3.message());
      // }
      
      // Map salesOrderMap = ret3.data as Map 
      // BigDecimal 订单待回款金额 = salesOrderMap["receivable_amount"] as BigDecimal
      // BigDecimal 订单已回款金额 = salesOrderMap["payment_amount"] as BigDecimal
 
      
  
    
    
    Map dzmxMap = [
        "field_sv3xo__c" : 回款计划,
        "field_Xya1L__c" : 计划回款日期,
        "field_U01EY__c" : 销售订单,
        "field_gw1Ef__c" : 合同编号,
        "field_9W91g__c" : 订单日期,
        "field_MGh87__c" : 订单金额,
        // "field_qpf12__c" : 订单待回款金额,
        // "field_vuwHu__c" : 订单已回款金额,
        "field_5c634__c" : 本期待回款金额,
        "field_m8ss5__c" : 回款计划类型,
        "owner" : 客户负责人,
        "record_type" : "record_yA607__c"
        ]
    
      对账明细列表.add(dzmxMap)
      逾期未付列表.add(dzmxMap)
    } 
  def (Boolean error54, String data54,String errorMessage54) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("returned_goods_inv_amount"),2, [["account_id": 客户名称],["field_7lNlO__c":Operator.GTE(月份开始日期)],["field_7lNlO__c":Operator.LT(月份结束日期)]])  
    BigDecimal 该月订单退货金额 = data54 as BigDecimal
    if(!该月订单退货金额) 该月订单退货金额 = 0
    
    
    
  def (Boolean error5, String data5,String errorMessage5) = Fx.object.aggregate("SalesOrderObj",
    Aggregate.SUM("field_vj4oM__c"),2, [["account_id": 客户名称],["order_time":Operator.GTE(月份开始日期)],["order_time":Operator.LT(月份结束日期)],["field_yE8st__c":Operator.IN(["YSD07","YSD01","YSD08"])],
    ["field_CLOq3__c":Operator.EXISTS(true)]])  
    BigDecimal 当月材料订单金额 = data5 as BigDecimal
    
  //当月退货金额
  def (Boolean error6, String data6,String errorMessage6) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["returned_goods_time":Operator.GTE(月份开始日期)],["returned_goods_time":Operator.LT(月份结束日期)]])  
    BigDecimal 当月退货金额 = data6 as BigDecimal
    if(!当月退货金额) 当月退货金额 = 0
    
  //当月收款金额
  def (Boolean error7, String data7,String errorMessage7) = Fx.object.aggregate("OrderPaymentObj",
    Aggregate.SUM("payment_amount"),2, [["account_id": 客户名称],["payment_time":Operator.GTE(月份开始日期)],["payment_time":Operator.LT(月份结束日期)]])  
    BigDecimal 当月回款金额 = data7 as BigDecimal
    if(!当月回款金额) 当月回款金额 = 0
  //当月退款金额
  def (Boolean error8, String data8,String errorMessage8) = Fx.object.aggregate("RefundObj",
    Aggregate.SUM("refunded_amount"),2, [["account_id": 客户名称],["refunded_time":Operator.GTE(月份开始日期)],["refunded_time":Operator.LT(月份结束日期)]])  
    BigDecimal 当月退款金额 = data8 as BigDecimal
    if(!当月退款金额) 当月退款金额 = 0
    
  //设备租赁回款计划待付金额合计 
  def (Boolean error11, String data11,String errorMessage11) = Fx.object.aggregate("SalesOrderObj",
    Aggregate.SUM("order_amount"),2, [["account_id": 客户名称],["field_yE8st__c":"YSD05"]]) 
    BigDecimal 设备租赁回款计划待付金额合计 = data11 as BigDecimal 
    if(!设备租赁回款计划待付金额合计) 设备租赁回款计划待付金额合计 = 0  
  
    def (Boolean error12, String data12,String errorMessage12) = Fx.object.aggregate("PaymentPlanObj",
  Aggregate.SUM("field_TOa0g__c"),2, [["account_id": 客户名称],["field_n6iq1__c":"q7xWQqzwJ"],["plan_payment_time":Operator.LT(月份结束日期)]])  
  BigDecimal 设备租赁已到期待付金额合计 = data12 as BigDecimal 
    if(!设备租赁已到期待付金额合计) 设备租赁已到期待付金额合计 = 0  
     
    def (Boolean error13, String data13,String errorMessage13) = Fx.object.aggregate("SalesOrderObj",
  Aggregate.SUM("order_amount"),2, [["account_id": 客户名称],["order_time":Operator.LT(月份结束日期)]])  
  BigDecimal 累计订单发货金额 = data13 as BigDecimal 
    if(!累计订单发货金额) 累计订单发货金额 = 0  
    
  def (Boolean error14, String data14,String errorMessage14) = Fx.object.aggregate("OrderPaymentObj",
  Aggregate.SUM("payment_amount"),2, [["account_id": 客户名称],["field_Zkxvg__c":Operator.LT(月份结束日期)]])  
  BigDecimal 累计回款金额 = data14 as BigDecimal 
    if(!累计回款金额) 累计回款金额 = 0    

  
  def (Boolean error15, String data15,String errorMessage15) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
  Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["returned_goods_time":Operator.LT(月份结束日期)]])  
  BigDecimal 累计退货金额 = data15 as BigDecimal 
    if(!累计退货金额) 累计退货金额 = 0    
  
  def (Boolean error90, String data90,String errorMessage90) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
  Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["returned_goods_time":Operator.GTE(月份结束日期)]])  
  BigDecimal 节点后退货金额 = data90 as BigDecimal 
    if(!节点后退货金额) 节点后退货金额 = 0   
    
  def (Boolean error16, String data16,String errorMessage16) = Fx.object.aggregate("RefundObj",
  Aggregate.SUM("refunded_amount"),2, [["account_id": 客户名称],["refunded_time":Operator.LT(月份结束日期)]])  
  BigDecimal 累计退款金额 = data16 as BigDecimal 
    if(!累计退款金额) 累计退款金额 = 0    
  
   def (Boolean error17, String data17,String errorMessage17) = Fx.object.aggregate("PaymentPlanObj",
  Aggregate.SUM("field_TOa0g__c"),2, [["account_id": 客户名称],["life_status":"normal"],["field_dUTxk__c":Operator.LT(月份结束日期)]])  
  BigDecimal 回款计划待回款金额合计 = data17 as BigDecimal 
    if(!回款计划待回款金额合计) 回款计划待回款金额合计 = 0    
  
  
  log.info("累计订单发货金额="+累计订单发货金额)
  log.info("累计回款金额="+累计回款金额)
  log.info("累计退货金额="+累计退货金额)
  log.info("累计退款金额="+累计退款金额)
  BigDecimal 客户总欠款 = 累计订单发货金额 - 累计回款金额  - 累计退货金额  - 节点后退货金额 + 累计退款金额
  
  
  def (Boolean error20,QueryResult data20,String errorMessage20) = Fx.object.find("object_1uK4B__c", [["field_7q55m__c" : 客户名称],["record_type" :"default__c"]], ["create_time":-1], 10, 0)
  if (error20) {
    log.info(errorMessage20)
  } 
  List 该客户以前的对账单 =  data20.dataList as List
  Map 上一次的对账单 = 该客户以前的对账单[0] as Map
  BigDecimal 上一期待付金额合计
  BigDecimal 上一期客户总欠款
  if(上一次的对账单){
    上一期待付金额合计 =上一次的对账单["field_21q6A__c"]
    上一期客户总欠款 = 上一次的对账单["field_wbTq2__c"]
  }else{
    上一期待付金额合计 = null
    上一期客户总欠款 = null
  }

    

  Map mainMap = [
      "field_ivD1v__c" : 当月退货金额,
      "field_wUJe5__c" : 当月回款金额,
      "field_4S6n1__c" : 当月退款金额,
      "field_w2M81__c":设备租赁回款计划待付金额合计,
      "field_7q55m__c" : 客户名称,
      "field_sM3Xo__c" : 对账月份,
      "owner" : 客户负责人,
      "record_type" : "default__c",
      "field_u3ehl__c" : 上一期待付金额合计,
      "field_Vl23i__c" : 上一期客户总欠款,
      "field_wbTq2__c" :客户总欠款,
      "field_0x615__c" :设备租赁已到期待付金额合计,
      "field_tpicl__c" : 对账单编码,
      "field_2eBA0__c" : 当月材料订单金额,
      "field_wmfI2__c" : toDay,
      "field_ISaBf__c" : 回款计划待回款金额合计
    ]
  
  Range range3 = Ranges.of(0,5)

  range3.each{

      it3 ->

    
  APIResult ret8 = Fx.object.find("SalesOrderObj", //查询发货单对象
            FQLAttribute.builder()
                    .columns(["_id", "name","field_J1ylq__c","field_vj4oM__c","field_xWng3__c","order_time"]) //返回的数据id
                    .queryTemplate( QueryTemplate.AND(["account_id": 客户名称] ,["order_time" : Operator.GTE(月份开始日期)],
                  ["order_time" : Operator.LT(月份结束日期)],["field_CLOq3__c":Operator.EXISTS(true)],["field_J1ylq__c":Operator.GT(0)]))
                  .orderBy(["order_time" : 1])
                  .skip(it3*100)
                  .limit(100)
                    .build(),
                SelectAttribute.builder()
                    .build());
    if(ret8.isError()){
        log.info(ret8.message());
    }
    
    List fhdList = ret8.data["dataList"] as List
    
   
    
    fhdList.each{ item8 ->
      Map fhdMap = item8 as Map
      String 发货单ID = fhdMap["_id"]  
      BigDecimal 发货数量 = fhdMap["field_J1ylq__c"] as BigDecimal 
      BigDecimal 发货金额 = fhdMap["field_vj4oM__c"] as BigDecimal 
      String ERP销售订单号 = fhdMap["field_xWng3__c"]
      Date 发货日期 = fhdMap["order_time"]
      Map fhmx = [
        "field_n09lz__c" : 发货数量,
        "field_f6qcq__c" : 发货单ID,
        "field_3kxcc__c" : 发货金额,
        "record_type" : "default__c",
        "field_Bj0w7__c" : ERP销售订单号,
        "field_qcbW0__c" : 发货日期
        ]
      发货明细列表.add(fhmx)
    }
    
    }
  
  APIResult ret9 = Fx.object.find("ReturnedGoodsInvoiceObj", //查询退货单对象
            FQLAttribute.builder()
                    .columns(["_id", "name","field_vj4oM__c","returned_goods_time","field_817aO__c","field_F2k17__c","field_7lNlO__c"]) //返回的数据id
                    .queryTemplate( QueryTemplate.AND(["account_id": 客户名称] ,["returned_goods_time" : Operator.GTE(月份开始日期)],
                  ["returned_goods_time" : Operator.LT(月份结束日期)],["field_7D2c9__c":Operator.EXISTS(true)],["field_J1ylq__c":Operator.GT(0)]))
                  .orderBy(["order_time" : 1])
                  .limit(100)
                    .build(),
                SelectAttribute.builder()
                    .build());
    if(ret9.isError()){
        log.info(ret9.message());
    }
  List 退货订单日期集合 = [] 
    List thdList = ret9.data["dataList"] as List
    thdList.each{ item10 ->
      Map 退货单项 = item10 as Map
      String 退货单ID = 退货单项["_id"]
      Date 退货日期 = 退货单项["returned_goods_time"] as Date
      Date 退货订单日期 = 退货单项["field_7lNlO__c"] as Date
      BigDecimal 合计退货金额 = 退货单项["field_817aO__c"] as BigDecimal
      // BigDecimal 退货数量 = 退货单项["field_F2k17__c"] as BigDecimal
      Map thdMx = [
        "field_Mgpyr__c" : 退货单ID,
        "field_c0re2__c" : 退货日期,
        "field_3STTv__c" : 合计退货金额,
        // "field_gBQaa__c" : 退货数量,
        "record_type" : "default__c",
        ]
      退货明细列表.add(thdMx)
      退货订单日期集合.add(退货订单日期)
    }
    
List 产生退货月份  = [] 
退货订单日期集合.each{item11 ->
  Date 退货订单日期 = item11 as Date
  Date 退货订单日期统一到一号 =  退货订单日期.withDay(1)  as Date
    if(!产生退货月份.contains(退货订单日期统一到一号)){
      产生退货月份.add(退货订单日期统一到一号)
    }
}
产生退货月份.remove(月份开始日期)

List 各月欠账 = []
List 各月欠款呈现 = []
List 包含的月份 = []
Map<Date, Map<String, BigDecimal>> 欠款累计 = [:]



逾期未付列表.each { item9 ->
    Date 统一到一号的回款日期 = (item9["field_Xya1L__c"] as Date).withDay(1)
    BigDecimal 本期应付金额 = new BigDecimal(item9["field_5c634__c"].toString())
    String 逾期回款计划类型 = item9["field_m8ss5__c"]

    if (!欠款累计.containsKey(统一到一号的回款日期)) {
        欠款累计[统一到一号的回款日期] = ["设备": BigDecimal.ZERO, "非设备": BigDecimal.ZERO]
    }

    if (逾期回款计划类型 == "Emi7sv3Ya") { // 设备销售回款计划
        欠款累计[统一到一号的回款日期]["设备"] += 本期应付金额
    } else {
        欠款累计[统一到一号的回款日期]["非设备"] += 本期应付金额
    }

    if (!包含的月份.contains(统一到一号的回款日期)) {
        包含的月份.add(统一到一号的回款日期)
    }
}


产生退货月份.each{item12 -> 
  Date 产生退货的月份 = item12 as Date
  if (!包含的月份.contains(产生退货的月份)) {
        包含的月份.add(产生退货的月份)
    }
}
log.info("包含的月份="+包含的月份)
包含的月份.each { 月份item ->
  log.info(月份item)
  Date 月份日期 = 月份item as Date
  Date 下月第一天 = 月份日期 + 1.months
  Date 上月月份日期 = 月份日期 - 1.months
  
  def (Boolean error50, String data50,String errorMessage50) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["field_2SzMq__c":"YSD06"],["field_7lNlO__c":Operator.GTE(月份日期)],["field_7lNlO__c":Operator.LT(下月第一天)],
    ["returned_goods_time":Operator.GTE(月份开始日期)],["returned_goods_time":Operator.LT(月份结束日期)]])  
    BigDecimal 该月设备订单退货金额 = data50 as BigDecimal
    if(!该月设备订单退货金额) 该月设备订单退货金额 = 0
    
  def (Boolean error51, String data51,String errorMessage51) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["field_2SzMq__c":Operator.NE("YSD06")],["field_7lNlO__c":Operator.GTE(月份日期)],["field_7lNlO__c":Operator.LT(下月第一天)],
    ["returned_goods_time":Operator.GTE(月份开始日期)],["returned_goods_time":Operator.LT(月份结束日期)]])  
    BigDecimal 该月非设备订单退货金额 = data51 as BigDecimal
    if(!该月非设备订单退货金额) 该月非设备订单退货金额 = 0
    BigDecimal 此月设备欠款
    BigDecimal 此月非设备欠款
    
    if(欠款累计[月份item]){
      此月设备欠款 = 欠款累计[月份item]["设备"] as BigDecimal
      此月非设备欠款 = 欠款累计[月份item]["非设备"] as BigDecimal
    }else{
      此月设备欠款  = 0
      此月非设备欠款 = 0
    }
    
    def (Boolean error52, String data52,String errorMessage52) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["field_2SzMq__c":"YSD06"],["field_7lNlO__c":Operator.GTE(上月月份日期)],["field_7lNlO__c":Operator.LT(月份日期)],
    ["returned_goods_time":Operator.GTE(上月月份日期)],["returned_goods_time":Operator.LT(月份日期)]])  
    BigDecimal 上月设备订单退货金额 = data52 as BigDecimal
    if(!上月设备订单退货金额) 上月设备订单退货金额 = 0
    
    def (Boolean error53, String data53,String errorMessage513) = Fx.object.aggregate("ReturnedGoodsInvoiceObj",
    Aggregate.SUM("field_817aO__c"),2, [["account_id": 客户名称],["field_2SzMq__c":Operator.NE("YSD06")],["field_7lNlO__c":Operator.GTE(上月月份日期)],["field_7lNlO__c":Operator.LT(月份日期)],
    ["returned_goods_time":Operator.GTE(上月月份日期)],["returned_goods_time":Operator.LT(月份日期)]])  
    BigDecimal 上月非设备订单退货金额 = data53 as BigDecimal
    if(!上月非设备订单退货金额) 上月非设备订单退货金额 = 0
    
    BigDecimal 本月设备退货前欠款 = 此月设备欠款 + 该月设备订单退货金额 + 上月设备订单退货金额
    BigDecimal 本月非设备退货前欠款 = 此月非设备欠款 + 该月非设备订单退货金额 + 上月非设备订单退货金额
    
    if(欠款累计[月份item] ){
    Map 月欠款明细 = [
        "field_BKvbv__c": 月份item,
        "field_ghmxw__c": 欠款累计[月份item]["设备"],
        "field_599nG__c": 欠款累计[月份item]["非设备"],
        "record_type" : "default__c"
    ]
    各月欠账.add(月欠款明细)  
    }
    Map 各月欠款呈现明细 = [
        "field_3qbg8__c" : 月份item,
        "field_xmXB2__c" : 本月设备退货前欠款,
        "field_6Sai9__c" : 本月非设备退货前欠款,
        "record_type" : "default__c"
      ]
    各月欠款呈现.add(各月欠款呈现明细)
    
    
}

log.info("各月欠款呈现="+各月欠款呈现)

    Map cdxMap = [
      "object_4uT76__c" : 对账明细列表,
      "object_2jkAf__c" : 发货明细列表,
      "object_mq7c1__c" : 各月欠账,
      "object_QnyRT__c" : 退货明细列表,
      "object_Ads21__c" : 各月欠款呈现,
      
      ]
    mainMap["field_G98Vg__c"]=该月订单退货金额
    Map createMap = [
      "object_1uK4B__c" : mainMap
      
      ]
      def(Boolean error10,Map data10,String errorMessage10) = Fx.object.create("object_1uK4B__c",mainMap,cdxMap,attribute)
      if(error10){
        log.info(errorMessage10)
        log.info(客户名字 +"创建对账单失败")
      }
}