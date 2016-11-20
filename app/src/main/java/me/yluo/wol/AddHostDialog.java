package me.yluo.wol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import me.yluo.wol.utils.NetUtil;


public class AddHostDialog extends DialogFragment implements View.OnClickListener {
    private EditText nickname, host, mac, port;
    private HostBean bean;
    private AddHostListener dialogListener;


    public static AddHostDialog newInstance(HostBean hostBean, AddHostListener l) {
        AddHostDialog frag = new AddHostDialog();
        if (hostBean == null) {
            String host = NetUtil.getLocalIp();
            if (TextUtils.isEmpty(host)) {
                host = "192.168.1.1";
            }
            hostBean = new HostBean(host);
        }
        frag.setHsot(hostBean);
        frag.setListner(l);
        return frag;
    }


    private void setListner(AddHostListener listener) {
        this.dialogListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_host, null);
        builder.setView(view);
        builder.setTitle("添加设备");
        host = (EditText) view.findViewById(R.id.host);
        nickname = (EditText) view.findViewById(R.id.nickname);
        mac = (EditText) view.findViewById(R.id.mac);
        port = (EditText) view.findViewById(R.id.port);
        host.setText(bean.host);
        if (TextUtils.isEmpty(bean.nickName)) {
            nickname.setText(bean.host);
        } else {
            nickname.setText(bean.nickName);
        }
        if (!TextUtils.isEmpty(bean.macAddr) && !bean.macAddr.equals(HostScanTask.NOMAC)) {
            mac.setText(MagicPacket.formatMac(bean.macAddr));
        }

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dialogListener = (AddHostListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private boolean checkInput() {
        bean.nickName = nickname.getText().toString();
        String portStr = port.getText().toString();
        bean.macAddr = mac.getText().toString();
        bean.host = host.getText().toString();

        if (TextUtils.isEmpty(bean.nickName)) {
            nickname.setError("主机名称不能为空");
            return false;
        }

        if (TextUtils.isEmpty(bean.host)) {
            host.setError("ip/域名不能未空");
            return false;
        }

        if (TextUtils.isEmpty(portStr)) {
            port.setError("端口号不能为空");
            return false;
        }
        bean.port = Integer.parseInt(portStr);

        if (!MagicPacket.validateMac(bean.macAddr)) {
            mac.setError("MAC地址不合法");
            return false;
        }

        return true;
    }

    public void setHsot(HostBean bean) {
        this.bean = bean;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_ok:
                if (checkInput()) {
                    dialogListener.onAddHostOkClick(null);
                    dismiss();
                }
                break;
        }
    }

    public interface AddHostListener {
        void onAddHostOkClick(HostBean bean);
    }
}
