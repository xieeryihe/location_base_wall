from django.http import HttpResponse, QueryDict
#from .settings import MEDIA_ROOT
from MyModel.models import *
from django.contrib.auth.decorators import login_required
from django.contrib import auth
import django
import json
import datetime
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger
import os
from django.db import connection
import math
from django.core.files import File as dFile
from PIL import Image
import random

media_url = 'http://121.43.110.176:8000/media/'

default_pictures_path = 'default_pictures/'

def hello(request):
    return HttpResponse("Hello world ! ")

def img_compress(img_path, crop=False):
    img = Image.open(img_path)
    width = img.width
    height = img.height
    rate = 1.0
    if width >= 2000 or height >= 2000:
      rate = 0.15
    elif width >= 1000 or height >= 1000:
      rate = 0.3
    elif width >= 500 or height >= 500:
      rate = 0.6
    width = int(width * rate)  # 新的宽
    height = int(height * rate) # 新的高

    img.thumbnail((width, height), Image.ANTIALIAS) # 生成缩略图
    if crop:
        # print(width, height)
        if height > width:
            left, right = 0, width
            up = (height - width) // 2
            down = up + width
        else:
            up, down = 0, height
            left = (width - height) // 2
            right = left + height
        box = (left, up, right, down)
        # print(box)
        img = img.crop(box)

    img.save(img_path)  # 保存到原路径
    

def register(request):
    """ username, password, phonenum, email """
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'id': None,
            'username': None,
        }
    }
    try:
        username = request.POST['username']
        password = request.POST['password']
        phonenum = request.POST['phonenum']
        email = request.POST['email']
        try:
            usr = User.objects.create(username=username, password=password, phonenum=phonenum, email=email)
            response_msg['data'] = {
                'id': usr.id,
                'username': username
            }
            
            # 设置默认头像
            df_pic_name = random.choice(os.listdir(default_pictures_path))
            with open(default_pictures_path + df_pic_name, 'rb') as f:
                usr.picture = dFile(f)
                usr.save()

        except django.core.exceptions.ValidationError:
            response_msg['code'] = 2
            response_msg['error_msg'] = '手机号已注册'
        except Exception as e:
            response_msg['code'] = 3
            response_msg['error_msg'] = '未知异常'
            # response_msg['error_msg'] = repr(e)
            # print(e)

    except Exception as e:
        response_msg['code'] = 1
        response_msg['error_msg'] = '注册信息不完全'
    finally:
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')


def edit_user(request):
    """ user_id, username, phonenum, email """
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'user_picture': None
        }
    }
    try:
        uid = request.POST['user_id'] # 必须有
        # 可以没有
        try:
            username = request.POST['username']
        except:
            username = None
        try:
            phonenum = request.POST['phonenum']
        except:
            phonenum = None
        try:
            email = request.POST['email']
        except:
            email = None
        try:
            picture = request.FILES.get('picture')
        except:
            picture = None
        usr = User.objects.get(id=uid)
        response_msg['data']['user_picture'] = media_url + usr.picture.name
        try:
            if username and username != '':
                usr.username = username
            if phonenum and phonenum != '':
                usr.phonenum = phonenum
            if email and email != '':
                usr.email = email
            if picture and picture != '':  # 修改头像
                if picture.name.split('.')[-1] not in ['jpg', 'jpeg', 'png', 'bmp']:
                    raise NotImplementedError
                usr.picture = picture
            usr.save()
            if picture:
                img_compress('media/' + usr.picture.name, crop=True)
                usr.save()
            response_msg['data']['user_picture'] = media_url + usr.picture.name

        except django.core.exceptions.ValidationError:
            response_msg['code'] = 2
            response_msg['error_msg'] = '手机号已注册'
        except NotImplementedError:
            response_msg['code'] = 4
            response_msg['error_msg'] = '头像文件格式错误'
        except Exception as e:
            response_msg['code'] = 3
            response_msg['error_msg'] = '未知异常'
            response_msg['error_msg'] = repr(e)

    except Exception as e:
        response_msg['code'] = 1
        response_msg['error_msg'] = '查不到用户'
    finally:
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')


def login(request):
    ret = {
        "code": 0, 
        "error_msg": "登陆成功", 
        "data": {
            "user_id": None,
            "username": None,
            "picture": None,
            "phonenum": None,
            "email": None,
            "is_manager": None
        }
    }
    try:
        user_phone = request.POST['userName']
        usr_password = request.POST['password']
    except:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    usr = User.objects.filter(phonenum=user_phone)
    if len(usr) == 0:
        ret["code"] = 2
        ret["error_msg"] = "用户名{}不存在".format(user_phone)
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
    usr = usr[0]
    # print(usr)
    if usr.password == usr_password:
        ret["data"] = {
            "user_id": usr.id,
            "username": usr.username,
            "picture": media_url + usr.picture.name,
            "phonenum": usr.phonenum,
            "email": usr.email,
            "is_manager": usr.is_manager
        }
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    ret["code"] = 3
    ret["error_msg"] = "密码错误"
    return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')





# 查看\创建帖子
def op_post(request):
    if request.method == 'GET':
        keyword = request.GET.get('key_words', '')
        if keyword == '':
            return get_posts_brief(request)
        else:
            return search_posts(request)
    elif request.method == 'POST':
        return create_post(request)
    else:
        ret = {
            'code': 7,
            'error_msg': '错误请求方法, 请用GET或者POST',
            'data': None
        }
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def create_post(request):
    """
    创建帖子, uid, title, content_type, text, location_x, location_y, ip_address
    其返回值包含了除media_url之外的所有的post内容
    """
    ret = {"code": 0, "error_msg": "发表成功", "data": None}
    try:
        uid = eval(request.POST['user_id'])
        title = request.POST['title']
        post_type = eval(request.POST['content_type'])
        post_text = request.POST['text']
        l_x = eval(request.POST['location_x'])
        l_y = eval(request.POST['location_y'])
        if post_type == 1:
            post_media = request.FILES.get('media')
            # print(post_media)
        else:
            post_media = None
        post_ip_address = request.POST['ip_address']
    except Exception as e:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
        print(str(e))
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try:
        u = User.objects.get(id=uid)
    except:
        ret["code"] = 2
        ret["error_msg"] = "用户不存在"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try: 
        # 先不创建 media_url
        post = Post.objects.create(uid=u, title=title, content_type=post_type, text=post_text, location_x=l_x, location_y=l_y, ip_address=post_ip_address)
        ret['data'] = {
            'id': post.id,
            'user_id': uid,
            'title': post.title,
            'content_type': post.content_type,
            'text': post_text,
            'media_url': '',
            'location_x': l_x,
            'location_y': l_y,
            'date': post.date.strftime("%Y-%m-%d %H:%M:%S"),
            'ip_address': post_ip_address
        }
        if post_type == 1: # 上传文件
            try:
                post.media_url=post_media
                post.save()
                post_media = media_url + post.media_url.name
                ret['data']['media_url'] = media_url + post.media_url.name

                file_type = post.media_url.name.split('.')[-1]
                # print(file_type)
                if file_type in ['png', 'jpg', 'jepg', 'gif', 'bmp']:
                    img_compress('media/' + post.media_url.name)
                    post.save()
                elif file_type in ['avi', 'mp4']:
                    pass
                else:
                    raise NotImplementedError
            except Exception as e:
                ret["code"] = 4
                # ret["error_msg"] = "上传文件失败"
                ret['error_msg'] = str(e)
                ret['data'] = None
                post.delete()
        for fn in os.listdir("post_cache/"):
            os.remove("post_cache/" + fn)
    except:
        ret["code"] = 3
        ret["error_msg"] = "发帖失败"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def get_posts_brief(request):
    """
    page_num, page_size, location_x, location_y, distance
    对于浏览所有帖子的应用来说, location_x, location_y 和 distance 不重要
    由于可能存在大量帖子, 发送回给前端会有较大延迟, 且会使得前端压力增大, 在
    这里需要进行分页。然而, 由于查询所有帖子的数据量过大, 重复查询代价太大, 
    则仅查询一次, 接下来利用缓存选取当前页帖子发送给前端
    """
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'items': [],  # 帖子列表
        }
        # 对于每条item，即每个帖子，返回 post_id, user_id, username, user_picture, title, short_text, date, location_x, location_y, ip_address
    }
    # item = {
    #     id: post.id,  # 帖子 id
    #     user_id: usr.id,  # 用户 id
    #     username: usr.username,  # 贴主用户名
    #     user_picture: usr.picture,  # 贴主头像
    #     title: post.title,  # 帖子标题
    #     text: head(post.text, 50),  # 帖子缩略内容
    #     date: post.date,  # 发帖日期
    #     location_x: post.location_x,  # 发表时的经度
    #     location_y: post.location_y,  # 发表时的维度
    #     ip_address: post.ip_address  # 发表时的ip地址
    # }
    try:
        page_num = int(request.GET.get('page_num', 1))
        page_size = int(request.GET.get('page_size', -1))
        ll_x = float(request.GET.get('location_x', 90.0))  # 经度
        ll_y = float(request.GET.get('location_y', 45.0))  # 维度
        post_dis = float(request.GET.get('distance', -1.0))
        if page_size <= 0:
            raise ValueError
    except:
        response_msg['code'] = 3
        response_msg['error_msg'] = '参数不正确'
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')

    # 查询所有帖子
    if post_dis < 0.:
        try:
            post_user_list = []
            # 在这里不执行查询
            with connection.cursor() as c:
                c.execute('SELECT MyModel_post.id, uid_id, username, picture, title, text, date, location_x, location_y, ip_address\
                          FROM MyModel_post join MyModel_user WHERE uid_id = MyModel_user.id ORDER BY MyModel_post.id DESC')
                posts_users = c.fetchall()
            # print(postes_users)
            for post_usr in posts_users:
                l_x = ll_x
                l_y = ll_y
                p_x = post_usr[7]
                p_y = post_usr[8] # 暂定为纬度
                    #if True:
                    # if p_y < l_y + 1 and p_y > l_y - 1 and (l_y >= 170 or l_y <= -170 or p_x < l_x + 1 and p_x > l_x - 1):
                l_x, l_y, p_x, p_y = map(math.radians, [l_x, l_y, p_x, p_y])
                d_x = l_x - p_x
                d_y = l_y - p_y
                a = math.sin(d_y/2)**2 + math.cos(l_y)*math.cos(p_y) * math.sin(d_x/2)**2
                c = 2 * math.asin(math.sqrt(a))
                dis = 6371.393 * c

                post_user_list.append({
                    'id': post_usr[0],  # 帖子 id
                    'user_id': post_usr[1],  # 用户 id
                    'username': post_usr[2],  # 贴主用户名
                    'user_picture': media_url + post_usr[3],  # 贴主头像
                    'title': post_usr[4],  # 帖子标题
                    'text': post_usr[5][:50],  # 帖子缩略内容
                    'date': post_usr[6].strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                    'location_x': post_usr[7],  # 发表时的经度
                    'location_y': post_usr[8],  # 发表时的维度
                    'ip_address': post_usr[9],  # 发表时的ip地址
                    'distance': round(dis, 1)
                })
        
            post_user_list_pages = Paginator(post_user_list, page_size)  # 通过分页，限制 mysql 的每次返回的数量为 page_size，在查询页数较小时速度很快
            single_post_user_list_page = post_user_list_pages.page(page_num)  # 而由于这里使用滑动刷新，很难到页数很大的场景，因此直接分页应该可以满足性能需求
            if not single_post_user_list_page.has_next():  # single_postes_page 为 Page 对象
                response_msg['code'] = 2
                response_msg['error_msg'] = '加载到底啦，没有更多帖子啦'
            if page_num == 1 and len(single_post_user_list_page) == 0:
                response_msg['code'] = 1
                response_msg['error_msg'] = '目前没有帖子噢'
            response_msg['data']['items'] = single_post_user_list_page.object_list

        except PageNotAnInteger and EmptyPage:
            response_msg['code'] = 4
            response_msg['error_msg'] = '页码错误'
        except Exception as e:
            response_msg['code'] = 5
            response_msg['error_msg'] = '查询帖子出错'
            # response_msg['error_msg'] = str(e)
            # print(str(e))
        finally:
            return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')
        
    else: # 查询附近帖子
        try:
            if not os.path.isfile("post_cache/" + str(ll_x) + str(ll_y) + str(post_dis) + ".json"):
                # make file, get post, save file
                post_user_list = []
                with connection.cursor() as c:
                    c.execute('SELECT MyModel_post.id, uid_id, username, picture, title, text, date, location_x, location_y, ip_address\
                              FROM MyModel_post join MyModel_user WHERE uid_id = MyModel_user.id  ORDER BY MyModel_post.id DESC')
                    posts_users = c.fetchall()
                for post_usr in posts_users:
                    l_x = ll_x
                    l_y = ll_y
                    p_x = post_usr[7]
                    p_y = post_usr[8] # 暂定为纬度
                    #if True:
                    if p_y < l_y + 1 and p_y > l_y - 1 and (l_y >= 170 or l_y <= -170 or p_x < l_x + 1 and p_x > l_x - 1):
                        l_x, l_y, p_x, p_y = map(math.radians, [l_x, l_y, p_x, p_y])
                        d_x = l_x - p_x
                        d_y = l_y - p_y
                        a = math.sin(d_y/2)**2 + math.cos(l_y)*math.cos(p_y) * math.sin(d_x/2)**2
                        c = 2 * math.asin(math.sqrt(a))
                        dis = 6371.393 * c
                        #print(dis)
                        if (dis <= post_dis):
                            post_user_list.append({
                                'id': post_usr[0],  # 帖子 id
                                'user_id': post_usr[1],  # 用户 id
                                'username': post_usr[2],  # 贴主用户名
                                'user_picture': media_url + post_usr[3],  # 贴主头像
                                'title': post_usr[4],  # 帖子标题
                                'text': post_usr[5][:50],  # 帖子缩略内容
                                'date': post_usr[6].strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                                'location_x': post_usr[7],  # 发表时的经度
                                'location_y': post_usr[8],  # 发表时的维度
                                'ip_address': post_usr[9],  # 发表时的ip地址
                                'distance': round(dis, 1)
                            })
                json_data = json.dumps(post_user_list, ensure_ascii=False, indent=4)
                with open("post_cache/" + str(ll_x) + str(ll_y) + str(post_dis) + ".json", "w",encoding = 'utf-8') as file:
                    file.write(json_data)

            # read file, get post
            with open("post_cache/" + str(ll_x) + str(ll_y) + str(post_dis) + ".json", "r",encoding = 'utf-8') as file:
                post_list = json.load(file)

            post_user_list_pages = Paginator(post_list, page_size)  # 通过分页，限制 mysql 的每次返回的数量为 page_size，在查询页数较小时速度很快
            single_post_user_list_page = post_user_list_pages.page(page_num)  # 而由于这里使用滑动刷新，很难到页数很大的场景，因此直接分页应该可以满足性能需求
            if not single_post_user_list_page.has_next():  # single_postes_page 为 Page 对象
                response_msg['code'] = 2
                response_msg['error_msg'] = '加载到底啦，没有更多帖子啦'
            if page_num == 1 and len(single_post_user_list_page) == 0:
                response_msg['code'] = 1
                response_msg['error_msg'] = '附近没有帖子噢'
            response_msg['data']['items'] = single_post_user_list_page.object_list

        except PageNotAnInteger and EmptyPage:
            response_msg['code'] = 4
            response_msg['error_msg'] = '页码错误'
        except Exception as e:
            response_msg['code'] = 5
            response_msg['error_msg'] = '查询帖子出错'
            # response_msg['error_msg'] = str(e)
            # print(str(e))
        finally:
            return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')



# 查看帖子详情/编辑帖子
def op_post_with_id(request):
    if request.method == 'GET':
        return get_post_detail(request)
    elif request.method == 'PUT':
        return edit_post(request)
    elif request.method == 'POST':
        return edit_post_media(request)
    else:
        ret = {
            'code': 7,
            'error_msg': '错误请求方法, 请用GET,PUT或POST',
            'data': None
        }
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def get_post_detail(request):
    """
    查看帖子详情, post_id
    返回的数据：
    'data' = {
        'id': post.id,
        'user_id': post.uid,
        'username': user.username,
        'user_picture': user.picture,
        'title': post.title,
        'content_type': post.content_type,
        'text': post.text,
        'media_url': post.media_url,
        'date': post.date,
        'location_x': post.location_x,
        'location_y': post.location_y,
        'ip_address': post.ip_address
    }
    """
    ret = {"code": 0, "error_msg": "查看详情成功", "data": None}
    try:
        post_id = int(request.GET.get('post_id', 0))
        if post_id <= 0:
            raise ValueError
        try:
            with connection.cursor() as c:
                c.execute('SELECT MyModel_post.id, uid_id, username, picture, title, content_type, text, media_url, date, location_x, location_y, ip_address\
                            FROM MyModel_post join MyModel_user WHERE MyModel_post.id = %s and uid_id = MyModel_user.id' % (post_id))
                post_user = c.fetchone()
            post_media_url = ['']
            if post_user[5] == 1:
                post_media_url = media_url + post_user[7],
            # print(post_media_url)
            ret['data'] = {
                'id': post_user[0],  # 帖子 id
                'user_id': post_user[1],  # 用户 id
                'username': post_user[2],  # 贴主用户名
                'user_picture': media_url + post_user[3],  # 贴主头像
                'title': post_user[4],  # 帖子标题
                'content_type': post_user[5], 
                'text': post_user[6],  # 帖子缩略内容
                'media_url': post_media_url[0],
                'date': post_user[8].strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                'location_x': post_user[9],  # 发表时的经度
                'location_y': post_user[10],  # 发表时的维度
                'ip_address': post_user[11]  # 发表时的ip地址
            }
        except Exception as e:
            ret["code"] = 2
            ret["error_msg"] = "查看详情失败, 可能帖子已被删除"
            # ret["error_msg"] = str(e)
            print(str(e))

    except:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


#edit_post(uid, pid, title, content_type, text, media)
def edit_post(request):
    ret = {"code": 0, "error_msg": "编辑成功", "data": {"items": []}}
    try:
        post_id = int(request.GET.get('post_id', -1))
        if post_id < 0:
            raise NotImplementedError
        args_list = request.body.split(b'\r\n')
        args = {}
        for i in range(0, len(args_list) - 5, 5):
            key = args_list[i+1].decode('utf-8').split('"')[1]
            value = args_list[i+4].decode('utf-8')
            args[key] = value

        post_uid = eval(args['user_id'])
        post_title = args['title']
        # post_content_type = eval(args['content_type'])
        post_text = args['text']

    except NotImplementedError:
        ret["code"] = 2
        ret["error_msg"] = "帖子id传参错误"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
    except  Exception as e:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
        # ret["error_msg"] = str(e)
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
    
    try:
        post = Post.objects.get(id=post_id)
        if post_uid != post.uid.id:
            ret["code"] = 3
            ret["error_msg"] = "用户无修改权限"
            return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
        # post.update(title=post_title, text=post_text)
        post.title = post_title
        post.text = post_text
        post.save()
        ret['data']['items'] = [{
                'id': post.id,  # 帖子 id
                'user_id': post_uid,  # 用户 id
                'title': post.title,  # 帖子标题
                'content_type': post.content_type,
                'media_url': media_url + post.media_url.name,
                'text': post.text,  # 帖子缩略内容
                'date': post.date.strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                'location_x': post.location_x,  # 发表时的经度
                'location_y': post.location_y,  # 发表时的维度
                'ip_address': post.ip_address  # 发表时的ip地址
            }]

    except:
        ret['code'] = 2
        ret['error_msg'] = '帖子不存在！'
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    post.update(title=post_title, text=post_text)
    post = post[0]
    ret['data']['items'] = [{
                'id': post.id,  # 帖子 id
                'user_id': post_uid,  # 用户 id
                'title': post.title,  # 帖子标题
                'content_type': post.content_type, 
                'text': post.text,  # 帖子缩略内容
                'date': post.date.strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                'location_x': post.location_x,  # 发表时的经度
                'location_y': post.location_y,  # 发表时的维度
                'ip_address': post.ip_address  # 发表时的ip地址
            }]
    return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def edit_post_media(request):
    ret = {"code": 0, "error_msg": "编辑成功", "data": {"items": []}}
    try:
        post_id = int(request.GET.get('post_id', -1))
        if post_id < 0:
            raise NotImplementedError
        post_media = request.FILES.get('media')
    except:
        ret["code"] = 1
        ret["error_msg"] = "参数错误"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
    
    try:
        post = Post.objects.get(id=post_id)
        # print(post_id, post_media)
        post.media_url = post_media
        post.content_type = 1
        post.save()
        # print("check")
        if post.media_url.name.split('.')[-1] in ['jpg', 'jpeg', 'png', 'bmp']:
            img_compress('media/' + post.media_url.name)
        # print("check")
        post.save()
        ret['data']['items'] = [{'media_url': media_url + post.media_url.name}]
    except:
        ret["code"] = 2
        ret["error_msg"] = "帖子不存在"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')



def op_comment(request):
    if request.method == 'GET':
        return show_comments(request)
    elif request.method == 'POST':
        return create_comment(request)
    else:
        ret = {
            'code': 7,
            'error_msg': '错误请求方法, 请用GET或者POST',
            'data': None
        }
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def create_comment(request):
    """创建评论, user_id, post_id, content_type, text, ip_address, media"""
    ret = {"code": 0, "error_msg": "发表成功", "data": None}
    # print(request.POST)
    try:
        comment_uid = int(request.POST['user_id'])
        comment_post_id = int(request.POST['post_id'])
        comment_content_type = int(request.POST['content_type'])
        comment_text = request.POST['text']
        comment_ip_address = request.POST['ip_address']
        if comment_content_type == 1:
            comment_media = request.FILES.get('media')
        else:
            comment_media = None
    except:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try:
        u = User.objects.get(id=comment_uid)
    except:
        ret["code"] = 2
        ret["error_msg"] = "用户不存在"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try:
        p = Post.objects.get(id=comment_post_id)
    except:
        ret["code"] = 3
        ret["error_msg"] = "贴子不存在"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try: 
        # 先不创建 media_url
        comment = Comment.objects.create(uid=u, pid=p, content_type=comment_content_type, text=comment_text, ip_address=comment_ip_address)
        ret['data'] = {
            'id': comment.id,
            'user_id': comment_uid,
            'post_id': comment_post_id,
            'content_type': comment.content_type,
            'media_url': '',
            'text': comment_text,
            'date': comment.date.strftime("%Y-%m-%d %H:%M:%S"),
            'ip_address': comment_ip_address
        }
        if comment_content_type == 1: # 上传文件
            try:
                comment.media_url = comment_media
                # print(comment_media)
                comment.save()
                ret['data']['media_url'] = media_url + comment.media_url.name

                file_type = comment.media_url.name.split('.')[-1]
                # print(file_type)
                if file_type in ['png', 'jpg', 'jepg', 'gif', 'bmp']:
                    img_compress('media/' + comment.media_url.name)
                    comment.save()
                elif file_type in ['avi', 'mp4']:
                    pass
                else:
                    raise NotImplementedError
            except:
                ret["code"] = 5
                ret["error_msg"] = "上传文件失败"
                # ret["error_msg"] = None
                comment.delete()
    except:
        ret["code"] = 4
        ret["error_msg"] = "评论失败"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def show_comments(request):
    """
    Method: GET. page_num, page_size, post_id
    return: {
        'code': ...,
        'error_msg': ...,
        'data': {
            'items': []
        }
    }
    for item in items:
    item = {
        'id': comment.id,
        'content_type': comment.content_type,
        'media_url': comment.media_url,
        'text': comment.text,
        'user_id': user.id,
        'username': user.username,
        'user_picture': user.picture,
        'date': comment.date,
        'ip_address': comment.ip_address
    }
    """
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'items': [],  # 帖子列表
        }
    }
    try:
        page_num = int(request.GET.get('page_num', 1))
        page_size = int(request.GET.get('page_size', -1))
        post_id = int(request.GET.get('post_id', -1))  # 帖子id
        if page_size <= 0 or post_id < 0:
            raise ValueError
    except:
        response_msg['code'] = 3
        response_msg['error_msg'] = '参数不正确'
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')

    try:
        comment_user_list = []
        # 在这里执行查询: 从post_id找到对应的评论，再结合到用户
        with connection.cursor() as c:
            c.execute('SELECT MyModel_comment.id, content_type, media_url, text, uid_id, username, picture, date, ip_address\
                        FROM MyModel_comment join MyModel_user WHERE pid_id = %s and uid_id = MyModel_user.id ORDER BY MyModel_comment.id DESC' % (post_id))
            comments_users = c.fetchall()
        # print(postes_users)
        for comment_user in comments_users:
            comment_media_url = ''
            if comment_user[1] == 1:
                comment_media_url = media_url + comment_user[2]
                if isinstance(comment_media_url, list) or isinstance(comment_media_url, tuple):
                    comment_media_url = comment_media_url[0]
            subcomment_count = len(SubComment.objects.filter(cid=comment_user[0]))
            comment_user_list.append({
                'id': comment_user[0],  # 评论 id
                'content_type': comment_user[1],  # 评论类型
                'media_url': comment_media_url,  # 媒体文件链接
                'text': comment_user[3],  # 评论内容
                'user_id': comment_user[4],  # 评论人id
                'username': comment_user[5],  # 评论人用户名
                'user_picture': media_url + comment_user[6],  # 评论人头像
                'date': comment_user[7].strftime("%Y-%m-%d %H:%M:%S"),  # 评论时间
                'ip_address': comment_user[8],  # 评论时的ip地址
                'subcomment_count': subcomment_count  # 子评论的数量
            })
    
        comment_user_list_pages = Paginator(comment_user_list, page_size)  # 通过分页，限制 mysql 的每次返回的数量为 page_size，在查询页数较小时速度很快
        single_comment_user_list_page = comment_user_list_pages.page(page_num)  # 而由于这里使用滑动刷新，很难到页数很大的场景，因此直接分页应该可以满足性能需求
        if not single_comment_user_list_page.has_next():  # single_postes_page 为 Page 对象
            response_msg['code'] = 2
            response_msg['error_msg'] = '加载到底啦，没有更多评论啦'
        if page_num == 1 and len(single_comment_user_list_page) == 0:
            response_msg['code'] = 1
            response_msg['error_msg'] = '目前没有评论噢'
        response_msg['data']['items'] = single_comment_user_list_page.object_list

    except PageNotAnInteger and EmptyPage:
        response_msg['code'] = 4
        response_msg['error_msg'] = '页码错误'
    except Exception as e:
        response_msg['code'] = 5
        response_msg['error_msg'] = '查询评论出错'
        # response_msg['error_msg'] = str(e)
        # print(str(e))
    finally:
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')





def op_subcomment(request):
    if request.method == 'GET':
        return show_subcomments(request)
    elif request.method == 'POST':
        return create_subcomment(request)
    else:
        ret = {
            'code': 7,
            'error_msg': '错误请求方法, 请用GET或者POST',
            'data': None
        }
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def show_subcomments(request):
    """
    Method: GET. page_num, page_size, comment_id
    return: {
        'code': ...,
        'error_msg': ...,
        'data': {
            'items': []
        }
    }
    for item in items:
    item = {
        'id': subcomment.id,
        'text': comment.text,
        'user_id': user.id,
        'username': user.username,
        'user_picture': user.picture,
        'date': comment.date,
        'ip_address': comment.ip_address
    }
    """
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'items': [],  # 帖子列表
        }
    }
    try:
        page_num = int(request.GET.get('page_num', 1))
        page_size = int(request.GET.get('page_size', -1))
        comment_id = int(request.GET.get('comment_id', -1))  # 帖子id
        if page_size <= 0 or comment_id < 0:
            raise ValueError
    except:
        response_msg['code'] = 3
        response_msg['error_msg'] = '参数不正确'
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')

    try:
        subcomment_user_list = []
        # 在这里执行查询: 从post_id找到对应的回复，再结合到用户
        with connection.cursor() as c:
            c.execute('SELECT MyModel_subcomment.id, text, uid_id, username, picture, date, ip_address\
                        FROM MyModel_subcomment join MyModel_user WHERE cid_id = %s and uid_id = MyModel_user.id ORDER BY MyModel_subcomment.id DESC' % (comment_id))
            subcomments_users = c.fetchall()
        # print(postes_users)
        for subcomment_user in subcomments_users:
            subcomment_user_list.append({
                'id': subcomment_user[0],  # 子评论 id
                'text': subcomment_user[1],  # 评论内容
                'user_id': subcomment_user[2],  # 评论人id
                'username': subcomment_user[3],  # 评论人用户名
                'user_picture': media_url + subcomment_user[4],  # 评论人头像
                'date': subcomment_user[5].strftime("%Y-%m-%d %H:%M:%S"),  # 评论时间
                'ip_address': subcomment_user[6]  # 评论时的ip地址
            })
    
        subcomment_user_list_pages = Paginator(subcomment_user_list, page_size)  # 通过分页，限制 mysql 的每次返回的数量为 page_size，在查询页数较小时速度很快
        single_subcomment_user_list_page = subcomment_user_list_pages.page(page_num)  # 而由于这里使用滑动刷新，很难到页数很大的场景，因此直接分页应该可以满足性能需求
        if not single_subcomment_user_list_page.has_next():  # single_postes_page 为 Page 对象
            response_msg['code'] = 2
            response_msg['error_msg'] = '加载到底啦，没有更多回复啦'
        if page_num == 1 and len(single_subcomment_user_list_page) == 0:
            response_msg['code'] = 1
            response_msg['error_msg'] = '目前没有回复噢'
        response_msg['data']['items'] = single_subcomment_user_list_page.object_list

    except PageNotAnInteger and EmptyPage:
        response_msg['code'] = 4
        response_msg['error_msg'] = '页码错误'
    except Exception as e:
        response_msg['code'] = 5
        response_msg['error_msg'] = '查询回复出错'
        # print(str(e))
    finally:
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')


#create_subcomment(uid, comment_id, text, ip_address)
def create_subcomment(request):
    ret = {"code": 0, "error_msg": "发表成功", "data": None}
    try:
        subcomment_uid = eval(request.POST['user_id'])
        subcomment_comment_id = eval(request.POST['comment_id'])
        subcomment_text = request.POST['text']
        subcomment_ip_address = request.POST['ip_address']
    except:
        ret["code"] = 1
        ret["error_msg"] = "参数不完整"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try:
        u =User.objects.get(id=subcomment_uid)
    except:
        ret["code"] = 2
        ret["error_msg"] = "用户不存在"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try:
        c = Comment.objects.get(id=subcomment_comment_id)
    except:
        ret["code"] = 3
        ret["error_msg"] = "评论不存在"
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

    try: 
        s = SubComment.objects.create(uid=u, cid=c, text=subcomment_text, ip_address=subcomment_ip_address)
        ret['date'] = {
            'id': s.id,
            'date': s.date.strftime("%Y-%m-%d %H:%M:%S")
        }
    except:
        ret["code"] = 4
        ret["error_msg"] = "评论失败"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

#search_post(key_words, page_num, page_size)
def search_posts(request):
    response_msg = {
        'code': 0,
        'error_msg': '',
        'data': {
            'items': [],  # 帖子列表
        }
    }
    try:
        key_words = request.GET.get('key_words', 0)
        page_num = int(request.GET.get('page_num', 0))
        page_size = int(request.GET.get('page_size', -1))
        ll_x = eval(request.GET.get('location_x', '90.0'))
        ll_y = eval(request.GET.get('location_y', '45.0'))
        if page_size <= 0:
            raise ValueError
    except Exception as e:
        response_msg['code'] = 3
        response_msg['error_msg'] = '参数不正确'
        response_msg['error_msg'] = str(e)
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')

    try:
        post_user_list = []
        # 在这里不执行查询
        with connection.cursor() as c:
            c.execute('SELECT MyModel_post.id, uid_id, username, picture, title, text, date, location_x, location_y, ip_address\
                      FROM MyModel_post join MyModel_user WHERE uid_id = MyModel_user.id')
            posts_users = c.fetchall()
        # print(postes_users)
        for post_usr in posts_users:
            if key_words in post_usr[4]:
                l_x = ll_x
                l_y = ll_y
                p_x = post_usr[7]
                p_y = post_usr[8] # 暂定为纬度
                    #if True:
                    # if p_y < l_y + 1 and p_y > l_y - 1 and (l_y >= 170 or l_y <= -170 or p_x < l_x + 1 and p_x > l_x - 1):
                l_x, l_y, p_x, p_y = map(math.radians, [l_x, l_y, p_x, p_y])
                d_x = l_x - p_x
                d_y = l_y - p_y
                a = math.sin(d_y/2)**2 + math.cos(l_y)*math.cos(p_y) * math.sin(d_x/2)**2
                c = 2 * math.asin(math.sqrt(a))
                dis = 6371.393 * c

                post_user_list.append({
                    'id': post_usr[0],  # 帖子 id
                    'user_id': post_usr[1],  # 用户 id
                    'username': post_usr[2],  # 贴主用户名
                    'user_picture': media_url + post_usr[3],  # 贴主头像
                    'title': post_usr[4],  # 帖子标题
                    'text': post_usr[5][:50],  # 帖子缩略内容
                    'date': post_usr[6].strftime("%Y-%m-%d %H:%M:%S"),  # 发帖日期
                    'location_x': post_usr[7],  # 发表时的经度
                    'location_y': post_usr[8],  # 发表时的维度
                    'ip_address': post_usr[9],  # 发表时的ip地址
                    'distance': round(dis, 1)
                })
    
        post_user_list_pages = Paginator(post_user_list, page_size)  # 通过分页，限制 mysql 的每次返回的数量为 page_size，在查询页数较小时速度很快
        single_post_user_list_page = post_user_list_pages.page(page_num)  # 而由于这里使用滑动刷新，很难到页数很大的场景，因此直接分页应该可以满足性能需求
        if not single_post_user_list_page.has_next():  # single_postes_page 为 Page 对象
            response_msg['code'] = 2
            response_msg['error_msg'] = '加载到底啦，没有更多帖子啦'
        if page_num == 1 and len(single_post_user_list_page) == 0:
            response_msg['code'] = 1
            response_msg['error_msg'] = '目前没有帖子噢'
        response_msg['data']['items'] = single_post_user_list_page.object_list

    except PageNotAnInteger and EmptyPage:
        response_msg['code'] = 4
        response_msg['error_msg'] = '页码错误'
    except Exception as e:
        response_msg['code'] = 5
        response_msg['error_msg'] = '查询帖子出错'
        response_msg['error_msg'] = str(e)
    finally:
        return HttpResponse(json.dumps(response_msg, ensure_ascii=False), content_type='application/json')

#delete_post(user_id, post_id)
def delete_post(request):
    ret = {"code": 0, "error_msg": "删除成功", "data": None}
    try:
        user_uid = int(request.POST['user_id'])
        post_id = int(request.POST['post_id'])
        if user_uid <= 0:
            raise ValueError
        if post_id <= 0:
            raise ValueError
        try:
            del_post = Post.objects.filter(id=post_id)
            if len(del_post) == 0:
                ret["code"] = 2
                ret["error_msg"] = "贴子不存在"
            else:
                user = User.objects.filter(id=user_uid)
                if del_post[0].uid.id != user_uid and user[0].is_manager != True:
                    ret["code"] = 1
                    ret["error_msg"] = "该用户没有权限"
                else:
                    Post.objects.filter(id=post_id).delete()
            for fn in os.listdir("post_cache/"):
                os.remove("post_cache/" + fn)
        except Exception as e:
            ret["code"] = 2
            ret["error_msg"] = "贴子不存在"
            # ret["error_msg"] = str(e)
            # print(str(e))

    except:
        ret["code"] = 3
        ret["error_msg"] = "参数不完整"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')

#delete_post(user_id, comment_id)
def delete_comment(request):
    ret = {"code": 0, "error_msg": "删除成功", "data": None}
    try:
        user_uid = int(request.POST['user_id'])
        comment_id = int(request.POST['comment_id'])
        if user_uid <= 0:
            raise ValueError
        if comment_id <= 0:
            raise ValueError
        try:
            del_comment = Comment.objects.filter(id=comment_id)
            if len(del_comment) == 0:
                ret["code"] = 2
                ret["error_msg"] = "评论不存在"
            else:
                post_user = del_comment[0].pid.uid.id
                user = User.objects.filter(id=user_uid)[0]
                if del_comment[0].uid.id != user_uid and post_user != user_uid and user.is_manager != True:
                    ret["code"] = 1
                    ret["error_msg"] = "该用户没有权限"
                else:
                    Comment.objects.filter(id=comment_id).delete()
        except Exception as e:
            ret["code"] = 2
            ret["error_msg"] = "评论不存在"
            # ret["error_msg"] = str(e)
            # print(str(e))

    except:
        ret["code"] = 3
        ret["error_msg"] = "参数不完整"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')


def delete_subcomment(request):
    ret = {"code": 0, "error_msg": "删除成功", "data": None}
    try:
        user_uid = int(request.POST['user_id'])
        subcomment_id = int(request.POST['subcomment_id'])
        if user_uid <= 0:
            raise ValueError
        if subcomment_id <= 0:
            raise ValueError
        try:
            del_subcomment = SubComment.objects.filter(id=subcomment_id)
            if len(del_subcomment) == 0:
                ret["code"] = 2
                ret["error_msg"] = "评论不存在"
            else:
                post_user = del_subcomment[0].cid.pid.uid.id
                comment_user = del_subcomment[0].cid.uid.id
                user = User.objects.filter(id=user_uid)[0]
                if del_subcomment[0].uid.id != user_uid and post_user != user_uid and comment_user != user_id and user.is_manager != True:
                    ret["code"] = 1
                    ret["error_msg"] = "该用户没有权限"
                else:
                    SubComment.objects.filter(id=subcomment_id).delete()
        except Exception as e:
            ret["code"] = 2
            ret["error_msg"] = "评论不存在"
            # ret["error_msg"] = str(e)
            # print(str(e))

    except:
        ret["code"] = 3
        ret["error_msg"] = "参数不完整"
    finally:
        return HttpResponse(json.dumps(ret, ensure_ascii=False), content_type='application/json')
