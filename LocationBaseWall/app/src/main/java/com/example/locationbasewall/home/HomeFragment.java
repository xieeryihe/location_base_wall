package com.example.locationbasewall.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationbasewall.R;
import com.example.locationbasewall.adapter.PostAdapter;
import com.example.locationbasewall.utils.DataGetter;
import com.example.locationbasewall.utils.LocalUserInfo;
import com.example.locationbasewall.utils.Location;
import com.example.locationbasewall.utils.MyToast;
import com.example.locationbasewall.utils.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postList; // 帖子数据列表

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Location location = new Location(getContext());
        location.getCurrentLocation(new Location.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String province, String city, String address) {
                System.out.println(city);
                System.out.println(address);

                LocalUserInfo localUserInfo = new LocalUserInfo(requireContext());
                String uid = localUserInfo.getId();

                // 获取地理位置之后才能发送数据请求
                int page_num = 1;
                int page_size = 10;
                double location_x = longitude;
                double location_y = latitude;
                int distance = -1;

                @SuppressLint("DefaultLocale")
                String targetUrl = String.format("" +
                        "http://121.43.110.176:8000/api/post?" +
                        "page_num=%d&page_size=%d&location_x=%.2f&location_y=%.2f&distance=%d",
                        page_num, page_size, location_x, location_y, distance);
                System.out.println(targetUrl);

                DataGetter.getDataFromServer(targetUrl, new DataGetter.DataGetterCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        try {
                            int code = jsonObject.getInt("code");
                            String errorMsg = jsonObject.getString("error_msg");
                            System.out.println("!!!our code :" + code);
                            if (code != 0 && code != 1 && code != 2) {
                                // 获取数据失败
                                String msg = "error code:" + code + "\nerror_msg" + errorMsg;
                                MyToast.show(getContext(),msg);
                            } else {
                                JSONObject data = jsonObject.getJSONObject("data");
                                MyToast.show(getContext(), "获取数据成功");

                                postList = new ArrayList<>(); // 初始化帖子数据列表

                                processHomePost(data,postList);

                                System.out.println("len-----------------------------------");
                                System.out.println(postList.size());
                                postAdapter = new PostAdapter(postList, new PostAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Post post) {
                                        // 处理点击事件，跳转到详情页或执行其他操作
                                        // 在此处启动 PostDetailActivity，并传递帖子数据
                                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                                        intent.putExtra("post_id", post.getId());
                                        intent.putExtra("publisher_id",post.getUid());
                                        startActivity(intent);
                                    }
                                });
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 更新UI组件的代码
                                        postAdapter.notifyDataSetChanged();
                                        recyclerView.setAdapter(postAdapter);
                                    }
                                });

                            }
                        } catch (JSONException e) {
                            MyToast.show(getContext(), "JSON错误");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        System.out.println(errorMessage);
                        MyToast.show(getContext(), "网络请求错误");
                    }
                });

            }
            @Override
            public void onLocationFailed(String errorMsg) {
                System.out.println("Failed to get location: " + errorMsg);

            }
        });

        return view;
    }
    public void processHomePost(JSONObject data, ArrayList<Post> postList) {

        try {

            JSONArray jsonArray = data.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                // 获取条目的各个字段值
                String id = item.getString("id");
                String uid = item.getString("user_id");
                String username = item.getString("username");
                String user_picture = item.getString("user_picture");
                String title = item.getString("title");
                String text = item.getString("text");
                String date = item.getString("date");
                double location_x = item.getDouble("location_x");
                double location_y = item.getDouble("location_y");
                String ip_address = item.getString("ip_address");
                System.out.println(user_picture);
                Post post = new Post(id, uid, username,user_picture, title, text, -1, "",date, location_x, location_y, ip_address);
                postList.add(post);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
