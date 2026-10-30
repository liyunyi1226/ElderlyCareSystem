export const tableSchemas = {
  elderly: {
    name: '老人档案表',
    deleteTip: '将<b>同时删除</b>该老人的工单、巡访记录、摔倒处置记录、求助事件和随访计划，并自动解绑其名下设备（设备保留）。',
    fields: [
      { key: 'name', label: '姓名', type: 'text', required: true },
      { key: 'gender', label: '性别', type: 'select', options: ['男', '女'], required: true },
      { key: 'age', label: '年龄', type: 'number' },
      { key: 'phone', label: '联系电话', type: 'text' },
      { key: 'address', label: '住址', type: 'text' },
      { key: 'emergencyContact', label: '紧急联系人', type: 'text' },
      { key: 'emergencyPhone', label: '紧急联系电话', type: 'text' },
      { key: 'chronicDiseases', label: '既往病史', type: 'textarea' },
      { key: 'allergies', label: '过敏史', type: 'textarea' },
      { key: 'currentIllness', label: '现病史', type: 'textarea' },
      { key: 'medications', label: '用药情况', type: 'textarea' },
      { key: 'bodyStatus', label: '身体现状', type: 'textarea' },
      { key: 'communityId', label: '所属社区ID（可不填）', type: 'number' }
    ]
  },
  orders: {
    name: '派医工单表',
    fields: [
      { key: 'orderNo', label: '工单编号', type: 'text', readonly: true },
      { key: 'elderlyId', label: '老人ID', type: 'number', required: true },
      { key: 'doctorId', label: '医生ID', type: 'number', required: true },
      { key: 'creatorId', label: '创建人ID', type: 'number' },
      { key: 'visitDate', label: '巡访日期', type: 'date' },
      { key: 'reason', label: '巡访事由', type: 'textarea' },
      { key: 'status', label: '状态', type: 'select', options: ['待分配医生', '待接收', '进行中', '已完成', '已退回'] }
    ]
  },
  visits: {
    name: '巡访记录表',
    fields: [
      { key: 'orderId', label: '工单ID', type: 'number', required: true },
      { key: 'elderlyId', label: '老人ID', type: 'number', required: true },
      { key: 'doctorId', label: '医生ID', type: 'number', required: true },
      { key: 'vitalSigns', label: '体征数据', type: 'textarea' },
      { key: 'bodyCondition', label: '身体状态', type: 'textarea' },
      { key: 'dailyLife', label: '生活起居', type: 'textarea' },
      { key: 'medicationSituation', label: '用药情况', type: 'textarea' },
      { key: 'subjectiveSymptoms', label: '老人主观症状', type: 'textarea' },
      { key: 'aiAdvice', label: 'AI建议', type: 'textarea' }
    ]
  },
  doctors: {
    name: '医生信息表',
    fields: [
      { key: 'name', label: '姓名', type: 'text', required: true },
      { key: 'gender', label: '性别', type: 'select', options: ['男', '女'] },
      { key: 'phone', label: '联系电话', type: 'text' },
      { key: 'specialization', label: '专长', type: 'text' },
      { key: 'hospital', label: '所属医院', type: 'text' },
      { key: 'communityId', label: '所属社区ID', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: ['在线', '忙碌', '离线'] }
    ]
  },
  users: {
    name: '用户账号表',
    deleteTip: '将<b>同时删除</b>该账号关联的医生资料或社区任职，以及其创建的工单（含巡访/处置记录）和随访计划。不能删除当前登录账号。',
    fields: [
      { key: 'username', label: '用户名', type: 'text', required: true },
      { key: 'password', label: '密码', type: 'text', required: true },
      { key: 'realName', label: '真实姓名', type: 'text', required: true },
      {
        key: 'role', label: '角色', type: 'select', required: true,
        options: [
          { label: '社区工作人员', value: 'community_worker' },
          { label: '系统管理员', value: 'system_admin' },
          { label: '医生', value: 'doctor' }
        ]
      },
      { key: 'phone', label: '电话', type: 'text' },
      {
        key: 'communityId', label: '所属社区', type: 'select',
        optionsRemote: 'communities',
        showOn: 'add',
        showWhen: { role: ['community_worker', 'doctor'] },
        requiredWhen: { role: ['community_worker'] },
        hint: '社区工作人员必选，医生可不选'
      }
    ]
  },
  communities: {
    name: '社区信息表',
    deleteTip: '将<b>解除</b>区内老人、医生的社区归属，并删除社区工作人员的任职记录；老人和医生档案本身会保留。',
    fields: [
      { key: 'name', label: '社区名称', type: 'text', required: true },
      { key: 'address', label: '社区地址', type: 'text' },
      { key: 'phone', label: '联系电话', type: 'text' }
    ]
  },
  devices: {
    name: '监护设备表',
    fields: [
      { key: 'deviceNo', label: '设备编号', type: 'text', required: true },
      { key: 'deviceType', label: '设备类型', type: 'select', options: ['跌倒监测', 'SOS求助'] },
      { key: 'installTime', label: '安装时间', type: 'date' },
      { key: 'status', label: '设备状态', type: 'select', options: ['正常', '离线', '故障'] },
      { key: 'elderlyId', label: '绑定老人ID（可不填）', type: 'number' }
    ]
  },
  events: {
    name: '求助事件表',
    fields: [
      { key: 'deviceId', label: '设备ID', type: 'number', required: true },
      { key: 'elderlyId', label: '老人ID', type: 'number', required: true },
      { key: 'eventType', label: '事件类型', type: 'select', options: ['摔倒告警', '手动呼救'] },
      { key: 'location', label: '事件地点', type: 'text' },
      { key: 'status', label: '状态', type: 'select', options: ['待处理', '已派单', '已处理'] }
    ]
  },
  followups: {
    name: '随访计划表',
    fields: [
      { key: 'elderlyId', label: '老人ID', type: 'number', required: true },
      { key: 'communityId', label: '社区ID（可不填）', type: 'number' },
      { key: 'creatorId', label: '创建人ID', type: 'number' },
      { key: 'planType', label: '计划类型', type: 'text' },
      { key: 'cycleDays', label: '随访周期（天）', type: 'number' },
      { key: 'startDate', label: '开始日期', type: 'date' },
      { key: 'nextVisitDate', label: '下次随访日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: ['进行中', '已结束'] }
    ]
  },
  staff: {
    name: '社区人员表',
    deleteTip: '将删除该工作人员的社区任职关系，其登录账号保留。',
    fields: [
      { key: 'communityId', label: '社区ID', type: 'number', required: true },
      { key: 'userId', label: '工作人员账号ID', type: 'number', required: true },
      { key: 'startDate', label: '入职日期', type: 'date' },
      { key: 'position', label: '职位', type: 'text' }
    ]
  },
  fallrecords: {
    name: '摔倒处置记录表',
    deleteTip: '将直接删除该条摔倒处置记录，此操作不可恢复。',
    fields: [
      { key: 'orderId', label: '工单ID', type: 'number', required: true },
      { key: 'eventId', label: '事件ID', type: 'number' },
      { key: 'elderlyId', label: '老人ID', type: 'number', required: true },
      { key: 'doctorId', label: '处置医生ID', type: 'number', required: true },
      { key: 'siteCondition', label: '现场情况', type: 'textarea' },
      { key: 'injurySite', label: '受伤部位', type: 'text' },
      { key: 'injurySeverity', label: '伤情程度', type: 'select', options: ['无', '轻', '中', '重'] },
      { key: 'disposalMeasure', label: '处置措施', type: 'textarea' },
      { key: 'needHospitalization', label: '是否送医', type: 'select', options: ['是', '否'] },
      { key: 'hospital', label: '送医医院', type: 'text' },
      { key: 'result', label: '处置结果', type: 'textarea' }
    ]
  }
}

export const tableColumns = {
  elderly: [
    { key: 'id', label: 'ID' },
    { key: 'name', label: '姓名' },
    { key: 'gender', label: '性别' },
    { key: 'age', label: '年龄' },
    { key: 'phone', label: '电话' },
    { key: 'address', label: '住址' },
    { key: 'bodyStatus', label: '身体现状' },
    { key: 'createTime', label: '创建时间', format: 'datetime' }
  ],
  orders: [
    { key: 'id', label: 'ID' },
    { key: 'orderNo', label: '工单编号' },
    { key: 'elderlyName', label: '老人' },
    { key: 'doctorName', label: '医生' },
    { key: 'eventType', label: '设备事件' },
    { key: 'visitDate', label: '巡访日期' },
    { key: 'status', label: '状态', format: 'status' },
    { key: 'createTime', label: '创建时间', format: 'datetime' }
  ],
  visits: [
    { key: 'id', label: 'ID' },
    { key: 'orderNo', label: '工单号' },
    { key: 'elderlyName', label: '老人' },
    { key: 'doctorName', label: '医生' },
    { key: 'vitalSigns', label: '体征数据' },
    { key: 'createTime', label: '创建时间', format: 'datetime' }
  ],
  doctors: [
    { key: 'id', label: 'ID' },
    { key: 'name', label: '姓名' },
    { key: 'gender', label: '性别' },
    { key: 'phone', label: '电话' },
    { key: 'specialization', label: '专长' },
    { key: 'hospital', label: '医院' },
    { key: 'communityName', label: '所属社区' }
  ],
  users: [
    { key: 'id', label: 'ID' },
    { key: 'username', label: '用户名' },
    { key: 'password', label: '密码' },
    { key: 'realName', label: '真实姓名' },
    { key: 'role', label: '角色', format: 'role' },
    { key: 'phone', label: '电话' },
    { key: 'status', label: '状态', format: 'userStatus' }
  ],
  communities: [
    { key: 'id', label: 'ID' },
    { key: 'name', label: '社区名称' },
    { key: 'address', label: '地址' },
    { key: 'phone', label: '联系电话' },
    { key: 'createTime', label: '创建时间', format: 'datetime' }
  ],
  devices: [
    { key: 'id', label: 'ID' },
    { key: 'deviceNo', label: '设备编号' },
    { key: 'deviceType', label: '类型' },
    { key: 'elderlyName', label: '绑定老人' },
    { key: 'status', label: '状态' },
    { key: 'installTime', label: '安装时间' },
    { key: 'createTime', label: '创建时间', format: 'datetime' }
  ],
  events: [
    { key: 'id', label: 'ID' },
    { key: 'eventType', label: '事件类型' },
    { key: 'elderlyName', label: '老人' },
    { key: 'deviceNo', label: '设备' },
    { key: 'location', label: '地点' },
    { key: 'eventTime', label: '事件时间', format: 'datetime' },
    { key: 'status', label: '状态' }
  ],
  followups: [
    { key: 'id', label: 'ID' },
    { key: 'elderlyName', label: '老人' },
    { key: 'planType', label: '计划类型' },
    { key: 'cycleDays', label: '周期(天)' },
    { key: 'startDate', label: '开始日期' },
    { key: 'nextVisitDate', label: '下次随访' },
    { key: 'communityName', label: '社区' },
    { key: 'status', label: '状态' }
  ],
  staff: [
    { key: 'id', label: 'ID' },
    { key: 'communityName', label: '社区' },
    { key: 'realName', label: '工作人员' },
    { key: 'userName', label: '账号' },
    { key: 'position', label: '职位' },
    { key: 'startDate', label: '入职日期' },
    { key: 'createTime', label: '绑定时间', format: 'datetime' }
  ],
  fallrecords: [
    { key: 'id', label: 'ID' },
    { key: 'orderNo', label: '工单号' },
    { key: 'eventType', label: '事件类型' },
    { key: 'elderlyName', label: '老人' },
    { key: 'doctorName', label: '处置医生' },
    { key: 'injurySite', label: '受伤部位' },
    { key: 'injurySeverity', label: '伤情' },
    { key: 'needHospitalization', label: '送医' },
    { key: 'hospital', label: '送医医院' },
    { key: 'result', label: '处置结果' },
    { key: 'createTime', label: '记录时间', format: 'datetime' }
  ]
}
