package com.jack.codelife;

// import some classs...

//this is a base task
public class BaseTask
{
	private final static Logger log = LoggerFactory.getLogger("BaseTask"); //log
	private String taskName; //任务名称
	private int  priority;//任务线程
	private BlockingQueue<Msg> msgQueue;//消息队列。装载需要处理的消息信息
	private Thread teskThread;//任务线程
	private final static int QUEUE_LENGTH =10000; //消息队列长度
	
	public Basetask(String taskName,int priority){
		this.taskName = taskName;
		this.priority=priority;
		msgQueue = new ArrayBlockingList<msg>(QUEUE_LENGTH);
		taskThread = new Thread(new Runnable(){
			try{
				BaskTask.this.runTask();
			}catch(Exception e){
				e.printStackTrace();
			}		 				
		}).start();
	}
	
	//开启线程。自动调用任务
	private void runTask(){
		//set name and priority of task...
		Thread.currentThread().setName(taskName);
		Thread.currentThread().setPriority(priority);
		//1.get msg -->2.handle msg      
		GMsg msg = null;
        while (true){
            try{
                msg = (GMsg) msgQueue.take();
            }  catch (InterruptedException e){
                e.printStackTrace();
                continue;
            }
            if (null == msg)
            {
                continue;
            }
            try
            {
                if (Constants.MSG_ID_SYS_KILL == msg.msgId || Constants.MSG_ID_SYS_QUICLY_KILL == msg.msgId)
                {
                    log.error("recv task kill msg(" + msg.msgId + "), task exit!!");
                    break;
                }
                handlerMsg(msg.msgId, msg.objContext);//子类会实现该方法
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
	
	}
	//子类需要继承的方法
	public abstract boolean startTask(){
	}

	public abstract boolean closeTask(){
	}

	public abstract void handleMsg(int msgId,Object objContext){

	}
	public boolean  addMsg(Msg msg){
			//log.info("add a msg:msgId--"msg.getMsgId());
		return msgQueue.offer(msg);
	}
	//子类可直接调用，向消息队列中添加消息
	public boolean addMsg(int msgId,Object objContext){
		Msg  msg = new Msg(msgId,objContext);
		return addMsg(msg);
		}
}

//this  is message class...
public class Msg{

private int msgId; //消息事件Id
 
private Object objContext; //消息事件内容


public Msg(int msgId,Object objContext){
	this.msgId=msgId;
	this.objContext=objContext;
}

//..getter and setter;
}