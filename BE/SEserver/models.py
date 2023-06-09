from django.db import models
from django.contrib.auth import settings
# Create your models here.
 
def user_picture_path(instance, filename):
    # 头像文件上传路径，将被上传到 MEDIA_ROOT/users_pic/user_<uid>/<filename>
    return 'users_pic/user_{0}/{1}'.format(instance.id, filename)

def post_media_path(instance, filename):
    # 发帖的媒体文件上传路径，将被上传到 MEDIA_ROOT/post_media/post_<pid>/<filename>
    return 'post_media/post_{0}/{1}'.format(instance.id, filename)

def comment_media_path(instance, filename):
    # 发帖的媒体文件上传路径，将被上传到 MEDIA_ROOT/post_media/post_<pid>/<filename>
    return 'post_media/post_{0}_comment/{1}_{2}'.format(instance.pid.id, instance.id, filename)

class User(models.Model):
    id = models.AutoField(primary_key=True)        # 自增主键，用户id
    picture = models.ImageField(null=True, blank=True, upload_to=user_picture_path)    # 头像
    username = models.CharField(max_length=50)      # 用户名
    password = models.CharField(max_length=100)      # 密码
    phonenum = models.CharField(max_length=20, unique=True)        # 手机号码
    email = models.EmailField(max_length=50)         # 邮件
    is_manager = models.BooleanField(default=False)        # 是否管理员，默认为False

class Post(models.Model):

    class TypeSuit(models.IntegerChoices):
        Text_Only = 0
        Media = 1

    id = models.BigAutoField(primary_key=True)        # 自增主键，帖子id
    uid = models.ForeignKey(User, on_delete=models.PROTECT)                  # 创建帖子的用户的id
    title = models.CharField(max_length=50)      # 帖子标题
    content_type = models.IntegerField(choices=TypeSuit.choices, default=0)    # 发帖类型，纯文本\带媒体
    text = models.CharField(max_length=1000)    # 帖子内容
    media_url = models.FileField(null=True, blank=True, upload_to=post_media_path)     # 媒体内容，可以为空
    date = models.DateTimeField(auto_now_add=True)       # 发帖日期，自动填写
    location_x = models.FloatField(null=True)                    # 经度
    location_y = models.FloatField(null=True)                    # 维度
    ip_address = models.CharField(max_length=50)       # ip地址

class Comment(models.Model):

    class TypeSuit(models.IntegerChoices):
        Text_Only = 0
        Media = 1

    id = models.BigAutoField(primary_key=True)       # 自增主键，对帖子的评论id
    pid = models.ForeignKey(Post, on_delete=models.CASCADE)                  # 所评论的帖子的id
    uid = models.ForeignKey(User, on_delete=models.PROTECT)                 # 评论的用户的id
    content_type = models.IntegerField(choices=TypeSuit.choices, default=0)    # 发帖类型，纯文本\带媒体
    media_url = models.FileField(null=True, blank=True, upload_to=comment_media_path)     # 媒体内容，可以为空
    text = models.CharField(max_length=1000)    #  评论内容
    date = models.DateTimeField(auto_now_add=True)   # 日期，自动填写
    ip_address = models.CharField(max_length=50)    # ip地址
    
class SubComment(models.Model):
    id = models.BigAutoField(primary_key=True)       # 自增主键，对评论的评论id
    cid = models.ForeignKey(Comment, on_delete=models.CASCADE)           # 所评论的评论的id
    uid = models.ForeignKey(User, on_delete=models.PROTECT)             # 评论的用户id
    text = models.CharField(max_length=1000)     # 评论内容
    date = models.DateTimeField(auto_now_add=True)      # 日期，自动填写
    ip_address = models.CharField(max_length=50)       # ip地址
