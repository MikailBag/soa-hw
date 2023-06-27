// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.28.1
// 	protoc        v3.12.4
// source: chat.proto

package pb

import (
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

type MessageData struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// author of the message
	ParticipantId string `protobuf:"bytes,1,opt,name=participant_id,json=participantId,proto3" json:"participant_id,omitempty"`
	Body          string `protobuf:"bytes,2,opt,name=body,proto3" json:"body,omitempty"`
	Eof           bool   `protobuf:"varint,3,opt,name=eof,proto3" json:"eof,omitempty"`
}

func (x *MessageData) Reset() {
	*x = MessageData{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *MessageData) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*MessageData) ProtoMessage() {}

func (x *MessageData) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use MessageData.ProtoReflect.Descriptor instead.
func (*MessageData) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{0}
}

func (x *MessageData) GetParticipantId() string {
	if x != nil {
		return x.ParticipantId
	}
	return ""
}

func (x *MessageData) GetBody() string {
	if x != nil {
		return x.Body
	}
	return ""
}

func (x *MessageData) GetEof() bool {
	if x != nil {
		return x.Eof
	}
	return false
}

type Topic struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	RoomId string `protobuf:"bytes,1,opt,name=room_id,json=roomId,proto3" json:"room_id,omitempty"`
	// "main" - available during day, readable for all, writable for all alive
	// "criminals" - always available, readable & writable for alive criminals
	// "dead" - always available, readable & writable for killed players
	StreamId string `protobuf:"bytes,2,opt,name=stream_id,json=streamId,proto3" json:"stream_id,omitempty"`
}

func (x *Topic) Reset() {
	*x = Topic{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Topic) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Topic) ProtoMessage() {}

func (x *Topic) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Topic.ProtoReflect.Descriptor instead.
func (*Topic) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{1}
}

func (x *Topic) GetRoomId() string {
	if x != nil {
		return x.RoomId
	}
	return ""
}

func (x *Topic) GetStreamId() string {
	if x != nil {
		return x.StreamId
	}
	return ""
}

type ServerInternalMessageDto struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Topic *Topic       `protobuf:"bytes,1,opt,name=topic,proto3" json:"topic,omitempty"`
	Data  *MessageData `protobuf:"bytes,2,opt,name=data,proto3" json:"data,omitempty"`
}

func (x *ServerInternalMessageDto) Reset() {
	*x = ServerInternalMessageDto{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *ServerInternalMessageDto) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*ServerInternalMessageDto) ProtoMessage() {}

func (x *ServerInternalMessageDto) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use ServerInternalMessageDto.ProtoReflect.Descriptor instead.
func (*ServerInternalMessageDto) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{2}
}

func (x *ServerInternalMessageDto) GetTopic() *Topic {
	if x != nil {
		return x.Topic
	}
	return nil
}

func (x *ServerInternalMessageDto) GetData() *MessageData {
	if x != nil {
		return x.Data
	}
	return nil
}

type SendRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Topic   *Topic       `protobuf:"bytes,1,opt,name=topic,proto3" json:"topic,omitempty"`
	Message *MessageData `protobuf:"bytes,2,opt,name=message,proto3" json:"message,omitempty"`
}

func (x *SendRequest) Reset() {
	*x = SendRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *SendRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*SendRequest) ProtoMessage() {}

func (x *SendRequest) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use SendRequest.ProtoReflect.Descriptor instead.
func (*SendRequest) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{3}
}

func (x *SendRequest) GetTopic() *Topic {
	if x != nil {
		return x.Topic
	}
	return nil
}

func (x *SendRequest) GetMessage() *MessageData {
	if x != nil {
		return x.Message
	}
	return nil
}

type SendResponse struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields
}

func (x *SendResponse) Reset() {
	*x = SendResponse{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[4]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *SendResponse) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*SendResponse) ProtoMessage() {}

func (x *SendResponse) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[4]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use SendResponse.ProtoReflect.Descriptor instead.
func (*SendResponse) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{4}
}

type WatchRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Topic         *Topic `protobuf:"bytes,1,opt,name=topic,proto3" json:"topic,omitempty"`
	ParticipantId string `protobuf:"bytes,2,opt,name=participant_id,json=participantId,proto3" json:"participant_id,omitempty"`
}

func (x *WatchRequest) Reset() {
	*x = WatchRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[5]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WatchRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WatchRequest) ProtoMessage() {}

func (x *WatchRequest) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[5]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WatchRequest.ProtoReflect.Descriptor instead.
func (*WatchRequest) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{5}
}

func (x *WatchRequest) GetTopic() *Topic {
	if x != nil {
		return x.Topic
	}
	return nil
}

func (x *WatchRequest) GetParticipantId() string {
	if x != nil {
		return x.ParticipantId
	}
	return ""
}

type WatchEvent struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Message *MessageData `protobuf:"bytes,1,opt,name=message,proto3" json:"message,omitempty"`
}

func (x *WatchEvent) Reset() {
	*x = WatchEvent{}
	if protoimpl.UnsafeEnabled {
		mi := &file_chat_proto_msgTypes[6]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WatchEvent) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WatchEvent) ProtoMessage() {}

func (x *WatchEvent) ProtoReflect() protoreflect.Message {
	mi := &file_chat_proto_msgTypes[6]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WatchEvent.ProtoReflect.Descriptor instead.
func (*WatchEvent) Descriptor() ([]byte, []int) {
	return file_chat_proto_rawDescGZIP(), []int{6}
}

func (x *WatchEvent) GetMessage() *MessageData {
	if x != nil {
		return x.Message
	}
	return nil
}

var File_chat_proto protoreflect.FileDescriptor

var file_chat_proto_rawDesc = []byte{
	0x0a, 0x0a, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12, 0x19, 0x63, 0x6f,
	0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61,
	0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x22, 0x5a, 0x0a, 0x0b, 0x4d, 0x65, 0x73, 0x73, 0x61,
	0x67, 0x65, 0x44, 0x61, 0x74, 0x61, 0x12, 0x25, 0x0a, 0x0e, 0x70, 0x61, 0x72, 0x74, 0x69, 0x63,
	0x69, 0x70, 0x61, 0x6e, 0x74, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0d,
	0x70, 0x61, 0x72, 0x74, 0x69, 0x63, 0x69, 0x70, 0x61, 0x6e, 0x74, 0x49, 0x64, 0x12, 0x12, 0x0a,
	0x04, 0x62, 0x6f, 0x64, 0x79, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x62, 0x6f, 0x64,
	0x79, 0x12, 0x10, 0x0a, 0x03, 0x65, 0x6f, 0x66, 0x18, 0x03, 0x20, 0x01, 0x28, 0x08, 0x52, 0x03,
	0x65, 0x6f, 0x66, 0x22, 0x3d, 0x0a, 0x05, 0x54, 0x6f, 0x70, 0x69, 0x63, 0x12, 0x17, 0x0a, 0x07,
	0x72, 0x6f, 0x6f, 0x6d, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x06, 0x72,
	0x6f, 0x6f, 0x6d, 0x49, 0x64, 0x12, 0x1b, 0x0a, 0x09, 0x73, 0x74, 0x72, 0x65, 0x61, 0x6d, 0x5f,
	0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x08, 0x73, 0x74, 0x72, 0x65, 0x61, 0x6d,
	0x49, 0x64, 0x22, 0x8e, 0x01, 0x0a, 0x18, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x49, 0x6e, 0x74,
	0x65, 0x72, 0x6e, 0x61, 0x6c, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x44, 0x74, 0x6f, 0x12,
	0x36, 0x0a, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x20,
	0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d,
	0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x54, 0x6f, 0x70, 0x69, 0x63,
	0x52, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x12, 0x3a, 0x0a, 0x04, 0x64, 0x61, 0x74, 0x61, 0x18,
	0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x26, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d,
	0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61,
	0x74, 0x2e, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x44, 0x61, 0x74, 0x61, 0x52, 0x04, 0x64,
	0x61, 0x74, 0x61, 0x22, 0x87, 0x01, 0x0a, 0x0b, 0x53, 0x65, 0x6e, 0x64, 0x52, 0x65, 0x71, 0x75,
	0x65, 0x73, 0x74, 0x12, 0x36, 0x0a, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x0b, 0x32, 0x20, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65,
	0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x54,
	0x6f, 0x70, 0x69, 0x63, 0x52, 0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x12, 0x40, 0x0a, 0x07, 0x6d,
	0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x26, 0x2e, 0x63,
	0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e,
	0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65,
	0x44, 0x61, 0x74, 0x61, 0x52, 0x07, 0x6d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x22, 0x0e, 0x0a,
	0x0c, 0x53, 0x65, 0x6e, 0x64, 0x52, 0x65, 0x73, 0x70, 0x6f, 0x6e, 0x73, 0x65, 0x22, 0x6d, 0x0a,
	0x0c, 0x57, 0x61, 0x74, 0x63, 0x68, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x12, 0x36, 0x0a,
	0x05, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x20, 0x2e, 0x63,
	0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e,
	0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x54, 0x6f, 0x70, 0x69, 0x63, 0x52, 0x05,
	0x74, 0x6f, 0x70, 0x69, 0x63, 0x12, 0x25, 0x0a, 0x0e, 0x70, 0x61, 0x72, 0x74, 0x69, 0x63, 0x69,
	0x70, 0x61, 0x6e, 0x74, 0x5f, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0d, 0x70,
	0x61, 0x72, 0x74, 0x69, 0x63, 0x69, 0x70, 0x61, 0x6e, 0x74, 0x49, 0x64, 0x22, 0x4e, 0x0a, 0x0a,
	0x57, 0x61, 0x74, 0x63, 0x68, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x12, 0x40, 0x0a, 0x07, 0x6d, 0x65,
	0x73, 0x73, 0x61, 0x67, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x26, 0x2e, 0x63, 0x6f,
	0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61,
	0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x44,
	0x61, 0x74, 0x61, 0x52, 0x07, 0x6d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x32, 0xc1, 0x01, 0x0a,
	0x0b, 0x43, 0x68, 0x61, 0x74, 0x53, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x12, 0x57, 0x0a, 0x04,
	0x53, 0x65, 0x6e, 0x64, 0x12, 0x26, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70,
	0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74,
	0x2e, 0x53, 0x65, 0x6e, 0x64, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x1a, 0x27, 0x2e, 0x63,
	0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e,
	0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x53, 0x65, 0x6e, 0x64, 0x52, 0x65, 0x73,
	0x70, 0x6f, 0x6e, 0x73, 0x65, 0x12, 0x59, 0x0a, 0x05, 0x57, 0x61, 0x74, 0x63, 0x68, 0x12, 0x27,
	0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d,
	0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63, 0x68, 0x61, 0x74, 0x2e, 0x57, 0x61, 0x74, 0x63, 0x68,
	0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x1a, 0x25, 0x2e, 0x63, 0x6f, 0x6d, 0x2e, 0x65, 0x78,
	0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x64, 0x65, 0x6d, 0x6f, 0x2e, 0x61, 0x70, 0x69, 0x2e, 0x63,
	0x68, 0x61, 0x74, 0x2e, 0x57, 0x61, 0x74, 0x63, 0x68, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x30, 0x01,
	0x42, 0x14, 0x5a, 0x12, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x63, 0x6f, 0x6d, 0x2f,
	0x73, 0x6f, 0x61, 0x2f, 0x70, 0x62, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_chat_proto_rawDescOnce sync.Once
	file_chat_proto_rawDescData = file_chat_proto_rawDesc
)

func file_chat_proto_rawDescGZIP() []byte {
	file_chat_proto_rawDescOnce.Do(func() {
		file_chat_proto_rawDescData = protoimpl.X.CompressGZIP(file_chat_proto_rawDescData)
	})
	return file_chat_proto_rawDescData
}

var file_chat_proto_msgTypes = make([]protoimpl.MessageInfo, 7)
var file_chat_proto_goTypes = []interface{}{
	(*MessageData)(nil),              // 0: com.example.demo.api.chat.MessageData
	(*Topic)(nil),                    // 1: com.example.demo.api.chat.Topic
	(*ServerInternalMessageDto)(nil), // 2: com.example.demo.api.chat.ServerInternalMessageDto
	(*SendRequest)(nil),              // 3: com.example.demo.api.chat.SendRequest
	(*SendResponse)(nil),             // 4: com.example.demo.api.chat.SendResponse
	(*WatchRequest)(nil),             // 5: com.example.demo.api.chat.WatchRequest
	(*WatchEvent)(nil),               // 6: com.example.demo.api.chat.WatchEvent
}
var file_chat_proto_depIdxs = []int32{
	1, // 0: com.example.demo.api.chat.ServerInternalMessageDto.topic:type_name -> com.example.demo.api.chat.Topic
	0, // 1: com.example.demo.api.chat.ServerInternalMessageDto.data:type_name -> com.example.demo.api.chat.MessageData
	1, // 2: com.example.demo.api.chat.SendRequest.topic:type_name -> com.example.demo.api.chat.Topic
	0, // 3: com.example.demo.api.chat.SendRequest.message:type_name -> com.example.demo.api.chat.MessageData
	1, // 4: com.example.demo.api.chat.WatchRequest.topic:type_name -> com.example.demo.api.chat.Topic
	0, // 5: com.example.demo.api.chat.WatchEvent.message:type_name -> com.example.demo.api.chat.MessageData
	3, // 6: com.example.demo.api.chat.ChatService.Send:input_type -> com.example.demo.api.chat.SendRequest
	5, // 7: com.example.demo.api.chat.ChatService.Watch:input_type -> com.example.demo.api.chat.WatchRequest
	4, // 8: com.example.demo.api.chat.ChatService.Send:output_type -> com.example.demo.api.chat.SendResponse
	6, // 9: com.example.demo.api.chat.ChatService.Watch:output_type -> com.example.demo.api.chat.WatchEvent
	8, // [8:10] is the sub-list for method output_type
	6, // [6:8] is the sub-list for method input_type
	6, // [6:6] is the sub-list for extension type_name
	6, // [6:6] is the sub-list for extension extendee
	0, // [0:6] is the sub-list for field type_name
}

func init() { file_chat_proto_init() }
func file_chat_proto_init() {
	if File_chat_proto != nil {
		return
	}
	if !protoimpl.UnsafeEnabled {
		file_chat_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*MessageData); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Topic); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*ServerInternalMessageDto); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[3].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*SendRequest); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[4].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*SendResponse); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[5].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WatchRequest); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_chat_proto_msgTypes[6].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WatchEvent); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_chat_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   7,
			NumExtensions: 0,
			NumServices:   1,
		},
		GoTypes:           file_chat_proto_goTypes,
		DependencyIndexes: file_chat_proto_depIdxs,
		MessageInfos:      file_chat_proto_msgTypes,
	}.Build()
	File_chat_proto = out.File
	file_chat_proto_rawDesc = nil
	file_chat_proto_goTypes = nil
	file_chat_proto_depIdxs = nil
}