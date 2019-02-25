# FileManager
说明：这是一个从U盘拷贝数据到AndroidTV的Dome
主要功能： 
      1.从U盘拷贝文件到AndroidTV的指定目录;
      2.对文件的重命名操作；
      3.监听U盘的插拔以及权限状态监听；
      4.对图片进行压缩显示，文件设有三级缓存。
主要问题：
      1.一次性加载了所有数据，占用内存过高；
      2.控制UI显示的代码逻辑健壮性不够。
      3.频繁访问本地数据。
