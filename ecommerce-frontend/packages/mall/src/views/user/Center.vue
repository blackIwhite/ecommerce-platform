<template>
  <div class="user-center">
    <h2>用户中心</h2>

    <div class="quick-links">
      <router-link to="/favorites" class="quick-link">我的收藏</router-link>
      <router-link to="/order" class="quick-link">我的订单</router-link>
      <router-link to="/coupons" class="quick-link">我的优惠券</router-link>
      <router-link to="/points" class="quick-link">积分中心</router-link>
      <router-link to="/aftersales" class="quick-link">售后服务</router-link>
      <router-link to="/ticket" class="quick-link">服务工单</router-link>
      <router-link to="/messages" class="quick-link">消息中心</router-link>
      <router-link to="/invoices" class="quick-link">我的发票</router-link>
      <router-link to="/browse-history" class="quick-link">浏览历史</router-link>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="profile">
        <div class="profile-section" v-loading="profileLoading">
          <el-form :model="profileForm" label-width="80px" style="max-width: 500px">
            <el-form-item label="手机号">
              <el-input :model-value="userInfo.phone" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="地址管理" name="address">
        <div class="address-section" v-loading="addressLoading">
          <el-button type="primary" @click="openAddressDialog()" style="margin-bottom: 16px">新增地址</el-button>

          <div class="address-list" v-if="addresses.length">
            <div class="address-item" v-for="addr in addresses" :key="addr.addressId">
              <div class="address-info">
                <div class="address-main">
                  <strong>{{ addr.receiverName }}</strong>
                  <span>{{ addr.receiverPhone }}</span>
                  <el-tag v-if="addr.isDefault === 1" size="small" type="danger">默认</el-tag>
                </div>
                <div class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</div>
              </div>
              <div class="address-actions">
                <el-button link type="primary" @click="openAddressDialog(addr)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteAddress(addr.addressId)">删除</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收货地址" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="addressDialogVisible" :title="editingAddress ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" required>
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" required>
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区/县" required>
          <el-input v-model="addressForm.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detailAddress" type="textarea" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@ecommerce/shared'
import type { UserDTO, UserAddressDTO } from '@ecommerce/shared'

const route = useRoute()
const activeTab = ref((route.query.tab as string) || 'profile')

const profileLoading = ref(true)
const profileSaving = ref(false)
const userInfo = reactive<UserDTO>({
  userId: 0,
  phone: '',
  nickname: '',
  avatar: '',
  status: 0,
  createTime: '',
})
const profileForm = reactive({ nickname: '' })

const addressLoading = ref(true)
const addressSaving = ref(false)
const addresses = ref<UserAddressDTO[]>([])
const addressDialogVisible = ref(false)
const editingAddress = ref<UserAddressDTO | null>(null)
const addressForm = reactive({
  addressId: 0,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0,
})

async function loadProfile() {
  profileLoading.value = true
  try {
    const me = await userApi.getMe()
    Object.assign(userInfo, me)
    profileForm.nickname = me.nickname || ''
  } catch {
    ElMessage.error('加载用户信息失败')
  } finally {
    profileLoading.value = false
  }
}

async function handleSaveProfile() {
  profileSaving.value = true
  try {
    await userApi.updateProfile({ nickname: profileForm.nickname })
    userInfo.nickname = profileForm.nickname
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    profileSaving.value = false
  }
}

async function loadAddresses() {
  addressLoading.value = true
  try {
    addresses.value = await userApi.getAddressList()
  } catch {
    addresses.value = []
  } finally {
    addressLoading.value = false
  }
}

function openAddressDialog(addr?: UserAddressDTO) {
  if (addr) {
    editingAddress.value = addr
    Object.assign(addressForm, {
      addressId: addr.addressId,
      receiverName: addr.receiverName,
      receiverPhone: addr.receiverPhone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detailAddress: addr.detailAddress,
      isDefault: addr.isDefault,
    })
  } else {
    editingAddress.value = null
    Object.assign(addressForm, {
      addressId: 0,
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: 0,
    })
  }
  addressDialogVisible.value = true
}

async function handleSaveAddress() {
  if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.detailAddress) {
    ElMessage.warning('请填写完整地址信息')
    return
  }
  addressSaving.value = true
  try {
    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault,
    }
    if (editingAddress.value) {
      await userApi.updateAddress({ ...data, addressId: addressForm.addressId })
      ElMessage.success('地址已更新')
    } else {
      await userApi.addAddress(data)
      ElMessage.success('地址已添加')
    }
    addressDialogVisible.value = false
    await loadAddresses()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    addressSaving.value = false
  }
}

async function handleDeleteAddress(addressId: number) {
  try {
    await ElMessageBox.confirm('确定删除此地址？', '提示')
    await userApi.deleteAddress(addressId)
    ElMessage.success('已删除')
    await loadAddresses()
  } catch {}
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadAddresses()])
})
</script>

<style scoped>
.user-center {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
.profile-section, .address-section {
  padding: 16px 0;
}
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #eaeaea;
  border-radius: 8px;
}
.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.address-detail {
  color: #666;
  font-size: 13px;
}
.address-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.quick-links {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.quick-link {
  padding: 8px 20px;
  border: 1px solid #eaeaea;
  border-radius: 6px;
  color: #333;
  text-decoration: none;
  transition: all 0.2s;
}
.quick-link:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
