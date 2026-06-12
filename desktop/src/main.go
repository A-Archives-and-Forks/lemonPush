package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"time"

	qrcodeTerminal "github.com/Baozisoftware/qrcode-terminal-go"
	"github.com/atotto/clipboard"
)

// 声明版本变量，用于构建过程中通过ldflags设置
var version string

var dt = time.Now()

// Config 应用配置结构体
type Config struct {
	Ip            string `json:"ip"`
	Port          int    `json:"port"`
	Auto_open_url bool   `json:"auto_open_url"`
	Folder        string `json:"folder"`
}

var config Config = Config{
	Ip:            "",
	Port:          14756,
	Auto_open_url: true,
	Folder:        "./_lemon_",
}

func main() {
	http.HandleFunc("/set_clipboard", setClipboard)
	http.HandleFunc("/get_clipboard", getClipboard)
	http.HandleFunc("/download", download)
	http.HandleFunc("/upload", upload)
	http.HandleFunc("/ping", ping)

	loadedConfig, lerr := initConfig("lemon_push.conf")
	if lerr != nil {
		fmt.Println("加载配置lemon_push.conf失败:", lerr)
		return
	}
	config = loadedConfig

	port := ":" + strconv.Itoa(config.Port) // 监听端口
	selectedIP := config.Ip                  // ip地址

	if config.Folder != "" {
		createFolderIfNotExists(config.Folder)
	}

	fmt.Println(dt.Format("2006-01-02 15:04:05"), "  服务端监听端口:", config.Port)

	if selectedIP == "" {
		localIPs := getLocalIP()
		fmt.Println(dt.Format("2006-01-02 15:04:05"), "  本机IP列表:")
		for i, ip := range localIPs {
			fmt.Printf("%d. %s\n", i+1, ip)
		}
		for {
			fmt.Print("输入序号选择一个IP地址(仅用于生成二维码): ")
			reader := bufio.NewReader(os.Stdin)
			input, _ := reader.ReadString('\n')
			input = strings.TrimSpace(input)
			index, err := strconv.Atoi(input)
			if err == nil && index >= 1 && index <= len(localIPs) {
				selectedIP = localIPs[index-1]
				break
			}
			fmt.Println("无效的选择，请重新输入.")
		}
	}

	fmt.Println(dt.Format("2006-01-02 15:04:05"), "  选择的IP地址:", selectedIP, " 请使用App扫码连接")
	url := selectedIP + port
	qRCode2ConsoleWithUrlNew(url)
	fmt.Println(dt.Format("2006-01-02 15:04:05"), "  服务端已启动")
	err := http.ListenAndServe(port, nil)
	if err != nil {
		fmt.Println("Error starting HTTP server:", err)
	}

}

func qRCode2ConsoleWithUrlNew(url string) {
	obj := qrcodeTerminal.New()
	obj.Get(url).Print()
}

func getLocalIP() []string {
	var ips []string
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println(err)
	}
	for _, address := range addrs {
		// 检查ip地址判断是否回环地址
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				ips = append(ips, ipnet.IP.String())
			}
		}
	}
	return ips
}

func openBrowser(url string) error {
	var cmd *exec.Cmd
	switch runtime.GOOS {
	case "darwin":
		cmd = exec.Command("open", url)
	case "linux":
		cmd = exec.Command("xdg-open", url)
	case "windows":
		cmd = exec.Command("cmd", "/c", "start", url)
	default:
		return fmt.Errorf("unsupported platform")
	}
	return cmd.Start()
}

func setClipboard(w http.ResponseWriter, r *http.Request) {
	values := r.URL.Query()
	code := values.Get("text")
	clipboard.WriteAll(code)
	fmt.Println("客户端 " + r.RemoteAddr + " 设置剪切板：" + code)
	if config.Auto_open_url {
		p := regexp.MustCompile(`https?://[^\s]+/[^/]+`)
		if p.MatchString(code) {
			matches := p.FindAllString(code, -1)
			for _, match := range matches {
				fmt.Printf("%s  启动浏览器打开链接：%s\n", dt.Format("2006-01-02 15:04:05"), match)
				openBrowser(match)
			}
		}
	}

	w.Header().Set("Content-Type", "application/json")
	resp := make(map[string]string)
	sendStr := "ok"
	resp["data"] = sendStr
	resp["code"] = "0"
	jsonResp, err := json.Marshal(resp)
	if err != nil {
		return
	}
	w.Write(jsonResp)
}

func getClipboard(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	resp := make(map[string]string)
	text, _ := clipboard.ReadAll()
	fmt.Println("客户端 " + r.RemoteAddr + " 获取剪切板：" + text)
	resp["data"] = text
	resp["code"] = "0"
	jsonResp, err := json.Marshal(resp)
	if err != nil {
		return
	}
	w.Write(jsonResp)
}

func download(w http.ResponseWriter, r *http.Request) {
	values := r.URL.Query()
	fileName := values.Get("filename")
	folderPath := config.Folder

	fmt.Println("客户端 " + r.RemoteAddr + " 下载文件：" + fileName)
	separator := string(filepath.Separator)
	file, err := os.Open(folderPath + separator + fileName)
	if err != nil {
		fmt.Println(err)
		http.Error(w, "文件未找到", http.StatusNotFound)
		return
	}
	defer file.Close()

	fileInfo, err := file.Stat()
	if err != nil {
		fmt.Println(err)
		http.Error(w, "文件信息无法获取", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Disposition", "attachment; filename="+fileName)
	w.Header().Set("Content-Type", "application/octet-stream")
	w.Header().Set("Content-Length", fmt.Sprint(fileInfo.Size()))

	_, copyErr := io.Copy(w, file)
	if copyErr != nil {
		fmt.Println(copyErr)
		http.Error(w, "文件无法下载", http.StatusInternalServerError)
		return
	}
}

func upload(w http.ResponseWriter, r *http.Request) {
	fmt.Println("客户端 " + r.RemoteAddr + " 上传文件")
	r.ParseMultipartForm(32 << 20)
	file, handler, err := r.FormFile("file")
	if err != nil {
		fmt.Println(err)
		return
	}
	defer file.Close()
	fmt.Println("文件名: " + handler.Filename)
	fmt.Println("文件大小: ", handler.Size)
	fmt.Println("MIME类型: " + handler.Header.Get("Content-Type"))
	// 创建一个目标文件
	separator := string(filepath.Separator)
	targetFile, err := os.Create(config.Folder + separator + handler.Filename)
	fmt.Println("文件路径: " + targetFile.Name())
	if err != nil {
		fmt.Println(err)
		return
	}
	defer targetFile.Close()

	// 将上传的文件内容拷贝到目标文件
	_, copyErr := io.Copy(targetFile, file)
	if copyErr != nil {
		fmt.Println(copyErr)
		return
	}

	resp := make(map[string]string)
	sendStr := "ok"
	resp["data"] = sendStr
	resp["code"] = "0"
	jsonResp, err := json.Marshal(resp)
	if err != nil {
		return
	}
	w.Write(jsonResp)
}

// ping 健康检查端点
func ping(w http.ResponseWriter, r *http.Request) {
	fmt.Println("客户端 " + r.RemoteAddr + " 在线检测")
	w.Header().Set("Content-Type", "application/json")
	resp := make(map[string]interface{})
	resp["code"] = 0
	resp["msg"] = "pong"
	resp["timestamp"] = time.Now().Unix()
	resp["service"] = "lemon_push"
	jsonResp, err := json.Marshal(resp)
	if err != nil {
		http.Error(w, "JSON编码错误", http.StatusInternalServerError)
		return
	}
	w.Write(jsonResp)
}

func createFolderIfNotExists(folderPath string) error {
	_, err := os.Stat(folderPath)
	if os.IsNotExist(err) {
		errDir := os.MkdirAll(folderPath, 0755) // 0755代表默认的文件夹权限
		if errDir != nil {
			return errDir
		}
		fmt.Println("文件夹不存在，已创建:", folderPath)
	} else {
		fmt.Println("文件夹已存在:", folderPath)
	}
	return nil
}

// initConfig 初始化配置（与 GUI 版本对齐，使用类型化 Config 结构体）
func initConfig(filename string) (Config, error) {
	execPath, err := os.Executable()
	if err != nil {
		return Config{}, err
	}
	execDir := filepath.Dir(execPath)
	filePath := filepath.Join(execDir, filename)

	file, err := os.Open(filePath)
	if err != nil {
		// 文件不存在，使用默认配置并创建文件
		defaultConfig := Config{
			Port:          14756,
			Folder:        "./_lemon_",
			Ip:            "",
			Auto_open_url: true,
		}

		// 创建文件并写入默认配置
		file, err = os.OpenFile(filePath, os.O_RDWR|os.O_CREATE, 0644)
		if err != nil {
			return Config{}, err
		}
		defer file.Close()

		boolStr := "false"
		if defaultConfig.Auto_open_url {
			boolStr = "true"
		}

		writer := bufio.NewWriter(file)
		_, err := writer.WriteString(fmt.Sprintf("ip=%s\nport=%d\nauto_open_url=%s\nfolder=%s\n",
			defaultConfig.Ip, defaultConfig.Port, boolStr, defaultConfig.Folder))
		if err != nil {
			return Config{}, err
		}
		writer.Flush()

		return defaultConfig, nil
	}
	defer file.Close()

	var cfg Config
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		parts := strings.SplitN(line, "=", 2)
		if len(parts) == 2 {
			key := strings.TrimSpace(parts[0])
			value := strings.TrimSpace(parts[1])

			switch key {
			case "ip":
				cfg.Ip = value
			case "port":
				port, err := strconv.Atoi(value)
				if err != nil {
					return Config{}, fmt.Errorf("invalid port value: %s", value)
				}
				cfg.Port = port
			case "auto_open_url":
				// 兼容旧配置（"open" 字符串）和新配置（"true"/"false"）
				b, err := strconv.ParseBool(value)
				if err != nil {
					// 旧版兼容: "open" 视为 true, 其他视为 false
					cfg.Auto_open_url = value == "open"
				} else {
					cfg.Auto_open_url = b
				}
			case "folder":
				cfg.Folder = value
			}
		}
	}

	if err := scanner.Err(); err != nil {
		return Config{}, err
	}

	return cfg, nil
}
